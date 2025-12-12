package me.dvyy.syncengine.client.sync

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import me.dvyy.sqlite.Database
import me.dvyy.sqlite.Transaction
import me.dvyy.sqlite.WriteTransaction
import me.dvyy.sqlite.statement.bindUuid
import me.dvyy.syncengine.actions.Action
import me.dvyy.syncengine.client.kvstore.KVStoreProperty
import me.dvyy.syncengine.client.mutators.ActionQueue
import me.dvyy.syncengine.client.mutators.RollbackJsonTable
import me.dvyy.syncengine.client.schema.ClientQueries
import me.dvyy.syncengine.reducers.Reducers
import me.dvyy.syncengine.schema.Schema
import me.dvyy.syncengine.sync.SyncRequest
import me.dvyy.syncengine.sync.SyncResult
import me.dvyy.syncengine.sync.SyncService
import me.dvyy.syncengine.sync.TableChanges
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Entrypoint for client syncengine.
 */
class SyncClient(
    private val logger: Logger,
    private val db: Database,
    private val actionQueue: ActionQueue,
    private val schema: Schema,
    private val syncService: SyncService,
) {
    private val clientDAO = ClientQueries()
    val syncedTables = schema.syncedTables.map { RollbackJsonTable(it) }
    val views = schema.views
    val lastFrameSeen = KVStoreProperty("lastFrameSeen")
    private val _deviceId = KVStoreProperty("deviceId")
    lateinit var deviceId: Uuid
    val changesMade = db.watch("actions_list") {}

    suspend operator fun invoke(action: Action) = actionQueue.invoke(action)

    suspend fun initialize() = db.write {
        clientDAO.create()
        syncedTables.forEach { it.create() }
        views.forEach { it.create() }
        _deviceId.getString() ?: _deviceId.setString(Uuid.random().toHexString())
        deviceId = Uuid.parseHex(_deviceId.getString()!!)
    }

    suspend fun establishSync() {
        var lastActionIdSent = -1L
        syncService.sync(
            deviceId,
            run {
                db.write {
                    actionQueue.preventPreviousReducing()
                }
                val request: SyncRequest = db.read {
                    getSyncRequest(lastActionIdSent)
                }
                lastActionIdSent = request.lastActionId
                request
            },
            db.watch("actions_list") { println("Detected action changes!") }.debounce(0.1.seconds).map {
                try {
                    db.write {
                        actionQueue.preventPreviousReducing()
                    }
                    val request: SyncRequest = db.read {
                        getSyncRequest(lastActionIdSent)
                    }
                    lastActionIdSent = request.lastActionId
                    request
                } catch (e: Error) {
                    logger.e(e) { "Failed to sync" }
                    throw e
                }
            }.filter { it.encodedActions.isNotEmpty() }.onEach {
                logger.v { "Syncing $it..." }
            }
        ).collect { updates ->
            try {
                logger.v { "Received sync response: $updates" }
                db.write {
                    reconcileDiff(updates)
                }
                logger.v { "Reconciled!" }
            } catch (e: Exception) {
                logger.e(e) { "Failed to sync!" }
            }
        }
    }

    suspend fun sync() {
        TODO()
//        val updates = syncService.sync(request)
    }

    context(tx: WriteTransaction)
    internal fun rollbackAll() {
        syncedTables.forEach { it.rollback() }
    }


    @OptIn(ExperimentalUuidApi::class)
    context(tx: Transaction)
    internal fun getSyncRequest(afterId: Long): SyncRequest {
        val start = if (afterId == -1L) actionQueue.firstMutatorId() else afterId + 1
        return SyncRequest(
            deviceId = Uuid.parseHex(_deviceId.getString()!!),
            lastFrameSeen = (lastFrameSeen.getString()?.toLong() ?: 0L),
            encodedActions = actionQueue.getAllEncodedAfter(start - 1),
            firstActionId = start,
        )
    }

    context(tx: WriteTransaction)
    internal fun applyTableChanges(changes: TableChanges) {
        val table = changes.table
        tx.prepare("INSERT OR REPLACE INTO $table (id, data, original_data, owner) VALUES (?, ?, NULL, ?)") {
            val insert = this
            tx.prepare("UPDATE $table SET original_data = NULL WHERE id = ?") {
                val update = this
                changes.changes.forEach { rowChange ->
                    logger.v { "Applying changes $rowChange" }
                    insert.bindUuid(1, rowChange.row)
                    insert.bindBlob(2, rowChange.data)
                    insert.bindLong(3, tx.identity)
                    insert.step()
                    insert.reset()
                    update.bindUuid(1, rowChange.row)
                    update.step()
                    update.reset()
                }
            }
        }
        tx.prepare("DELETE FROM $table WHERE id = :id") {
            changes.deletions.forEach { deleted ->
                logger.v { "Deleting $deleted" }
                bindUuid(1, deleted)
                step()
                reset()
            }
        }
    }

    context(tx: WriteTransaction)
    internal fun reconcileDiff(updates: SyncResult) {
        rollbackAll()
        logger.v { "Clearing up to ${updates.lastActionIdApplied}" }
        actionQueue.clearAcknowledged(updates.lastActionIdApplied)
        updates.changes.forEach { applyTableChanges(it) }
        lastFrameSeen.setString(updates.serverFrame.toString())
        logger.v { "Reconciling with local changes..." }
        actionQueue.invokeAllStored()
    }

    companion object {
        fun of(
            db: Database,
            reducers: Reducers,
            schema: Schema,
            syncService: SyncService,
            logger: Logger = Logger.withTag("Client"),
        ) = SyncClient(logger, db, ActionQueue(logger, db, schema, reducers), schema, syncService)
    }
}
