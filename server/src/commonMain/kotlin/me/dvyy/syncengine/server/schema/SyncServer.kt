@file:OptIn(ExperimentalTime::class)

package me.dvyy.syncengine.server.schema

import co.touchlab.kermit.Logger
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import me.dvyy.sqlite.Database
import me.dvyy.sqlite.Identity
import me.dvyy.sqlite.Transaction
import me.dvyy.sqlite.WriteTransaction
import me.dvyy.sqlite.statement.getUuid
import me.dvyy.syncengine.reducers.Reducers
import me.dvyy.syncengine.schema.JsonView
import me.dvyy.syncengine.schema.Schema
import me.dvyy.syncengine.sync.RowChange
import me.dvyy.syncengine.sync.SyncRequest
import me.dvyy.syncengine.sync.SyncResult
import me.dvyy.syncengine.sync.TableChanges
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class SyncServer(
    private val logger: Logger,
    private val db: Database,
    private val schema: Schema,
    private val actionProcessor: ServerActionProcessor,
) {
    val syncedTables: List<UserRestrictedJsonTable> = schema.syncedTables.map { UserRestrictedJsonTable(it) }
    val views: List<JsonView> = schema.views
    private val _updates = MutableSharedFlow<Unit>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    ).apply { tryEmit(Unit) }
    val updates = _updates.asSharedFlow()
    val dao = ServerQueries()

    suspend fun getLastActionApplied(uuid: Uuid, identity: Identity): Long = db.read {
        dao.clients.getLastActionApplied(uuid, identity)
            .firstOrNull { getLong(it.last_action_applied) }
            ?: -1
    }

    suspend fun getUpdates(
        uuid: Uuid,
        identity: Identity,
        lastFrameSeen: Long,
    ): SyncResult {
        return db.write(identity = identity) {
            val lastApplied =
                dao.clients.getLastActionApplied(uuid, identity).firstOrNull { getLong(it.last_action_applied) }
                    ?: -1
            SyncResult(
                lastActionIdApplied = lastApplied,
                changes = getUpdatedSince(lastFrameSeen),
                serverFrame = getServerFrame(),
            )
        }
    }

    fun streamingSync(
        user: Identity,
        initialRequest: SyncRequest,
        request: Flow<SyncRequest>,
    ): Flow<SyncResult> = channelFlow {
        val initialResult = sync(initialRequest, user)
        send(initialResult)
        var lastFrameSent = initialResult.serverFrame

        // Listen to incoming requests
        launch {
            request.collect {
                logger.v { "Received sync request: $it" }
                if (it.encodedActions.isNotEmpty()) sync(it, user)
            }
        }

        // Respond back with all server updates (including from other clients)
        updates.collect {
            val result = getUpdates(uuid = initialRequest.deviceId, identity = user, lastFrameSeen = lastFrameSent)
            lastFrameSent = result.serverFrame
            if (result.changes.isNotEmpty()) send(result)
        }
    }

    suspend fun sync(
        request: SyncRequest,
        identity: Identity,
    ): SyncResult {
        if (request.encodedActions.isNotEmpty()) {
            // Apply actions
            db.write(identity = identity) {
                incrementServerFrame()
                actionProcessor.invokeActions(request)
                //TODO write to DB which action was last applied so it can be skipped in case of a retry
            }
            logger.v { "Applied ${request.encodedActions.size} actions from ${request.deviceId}" }
            _updates.emit(Unit)
        }

        //TODO maybe get this back from applier for brevity
        val lastApplied = request.firstActionId + request.encodedActions.lastIndex

        return db.read(identity = identity) {
            val serverFrame = getServerFrame()
            SyncResult(
                lastActionIdApplied = lastApplied,
                changes = getUpdatedSince(request.lastFrameSeen),
                serverFrame = serverFrame,
            )
        }
    }

    suspend fun initialize() = db.write {
        syncedTables.forEach { it.create() }
        views.forEach { it.create() }
        ServerQueries().create()
    }

    @OptIn(ExperimentalUuidApi::class)
    context(tx: Transaction)
    internal fun getUpdatedSince(frame: Long): List<TableChanges> = schema.syncedTables.map { table ->
        TableChanges(
            table = table.name,
            changes = tx.select(
                "SELECT id, data FROM $table WHERE frame > ? AND owner = ? AND data IS NOT null",
                frame,
                tx.identity
            )
                .map {
                    RowChange(
                        row = getUuid(0),
                        data = getBlob(1),
                    )
                },
            deletions = tx.select(
                "SELECT id, data FROM $table WHERE frame > ? AND owner = ? AND data IS null",
                frame,
                tx.identity
            ).map {
                getUuid(0)
            }
        )
    }

    //TODO consider any way of masking server frame from users when no changes relevant to them occur.
    context(tx: Transaction)
    internal fun getServerFrame(): Long {
        return tx.select("SELECT value FROM syncengine_store WHERE key = 'frame'").firstOrNull { getLong(0) } ?: 0L
    }

    context(tx: WriteTransaction)
    internal fun incrementServerFrame() {
        val frame = getServerFrame()
        tx.exec("INSERT OR REPLACE INTO syncengine_store VALUES ('frame', ?)", frame + 1)
    }

    companion object {
        fun of(
            db: Database,
            reducers: Reducers,
            schema: Schema,
            logger: Logger = Logger.withTag("Server"),
        ) = SyncServer(logger, db, schema, ServerActionProcessor(logger, schema, reducers))
    }
}
