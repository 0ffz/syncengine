@file:OptIn(ExperimentalTime::class)

package me.dvyy.syncengine

import me.dvyy.sqlite.Database
import me.dvyy.sqlite.Identity
import me.dvyy.sqlite.Transaction
import me.dvyy.sqlite.WriteTransaction
import me.dvyy.sqlite.statement.getUuid
import me.dvyy.sqlite.tables.View
import me.dvyy.syncengine.schema.Schema
import me.dvyy.syncengine.schema.UserRestrictedJsonTable
import me.dvyy.syncengine.sync.*
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

class SyncServer(
    private val db: Database,
    private val schema: Schema,
    private val applier: MutatorApplier<*, *>,
) {
    val syncedTables: List<UserRestrictedJsonTable> = schema.syncedTables.map { UserRestrictedJsonTable(it) }
    val views: List<View> = schema.views

    suspend fun sync(
        request: SyncRequest,
        identity: Identity,
    ): SyncResult {
        // Apply mutations
        db.write(identity = identity) {
            applier.applyMutations(request)
            incrementServerFrame()
            //TODO write to DB which mutation was last applied so it can be skipped in case of a retry
        }

        //TODO maybe get this back from applier for brevity
        val lastApplied = request.firstMutatorId + request.encodedMutators.size

        return db.read(identity = identity) {
            SyncResult(
                lastMutatorIdApplied = lastApplied,
                changes = getUpdatedSince(request.lastFrameSeen),
                serverFrame = getServerFrame(),
            )
        }
    }

    internal suspend fun initialize() = db.write {
        syncedTables.forEach { it.create() }
        views.forEach { it.create() }
    }

    @OptIn(ExperimentalUuidApi::class)
    context(tx: Transaction)
    internal fun getUpdatedSince(frame: Long): List<TableChanges> = schema.syncedTables.map { table ->
        TableChanges(
            table = table.name,
            changes = tx.select("SELECT uuid, data FROM $table WHERE owner = ? AND frame >= ?", tx.identity, frame)
                .map {
                    RowChange(
                        row = getUuid(0),
                        data = getBlob(1),
                    )
                }
        )
    }

    //TODO consider any way of masking server frame from users when no changes relevant to them occur.
    context(tx: Transaction)
    internal fun getServerFrame(): Long {
        TODO()
    }

    context(tx: WriteTransaction)
    internal fun incrementServerFrame(): Long {
        TODO()
    }
}

fun SyncServer.mockService(user: Identity) = object : SyncService {
    override suspend fun sync(request: SyncRequest): SyncResult {
        return sync(request, user)
    }
}
