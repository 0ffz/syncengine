package me.dvyy.syncengine.client.sync

import me.dvyy.sqlite.Database
import me.dvyy.sqlite.Transaction
import me.dvyy.sqlite.WriteTransaction
import me.dvyy.sqlite.statement.bindUuid
import me.dvyy.syncengine.client.kvstore.KVStoreProperty
import me.dvyy.syncengine.client.mutators.ActionQueue
import me.dvyy.syncengine.client.mutators.RollbackJsonTable
import me.dvyy.syncengine.client.schema.ClientQueries
import me.dvyy.syncengine.schema.Schema
import me.dvyy.syncengine.sync.SyncRequest
import me.dvyy.syncengine.sync.SyncResult
import me.dvyy.syncengine.sync.SyncService
import me.dvyy.syncengine.sync.TableChanges
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Entrypoint for client syncengine.
 */
@OptIn(ExperimentalUuidApi::class)
class SyncClient(
    private val db: Database,
    private val actionQueue: ActionQueue,
    private val schema: Schema,
    private val syncService: SyncService,
) {
    private val clientDAO = ClientQueries()
    val syncedTables = schema.syncedTables.map { RollbackJsonTable(it) }
    val views = schema.views
    val lastFrameSeen = KVStoreProperty("lastFrameSeen")
    val deviceId = KVStoreProperty("deviceId")
    val changesMade = db.watch("actions_list") {}

    suspend fun initialize() = db.write {
        clientDAO.create()
        syncedTables.forEach { it.create() }
        views.forEach { it.create() }
        deviceId.setString(Uuid.random().toHexString())
    }

    suspend fun sync() {
        val request: SyncRequest = db.read {
            getSyncRequest()
        }
        val updates = syncService.sync(request)

        db.write {
            reconcileDiff(updates)
        }
    }

    context(tx: WriteTransaction)
    internal fun rollbackAll() {
        syncedTables.forEach { it.rollback() }
    }


    @OptIn(ExperimentalUuidApi::class)
    context(tx: Transaction)
    internal fun getSyncRequest() = SyncRequest(
        deviceId = Uuid.parseHex(deviceId.getString()!!),
        lastFrameSeen = (lastFrameSeen.getString()?.toLong() ?: 0L),
        encodedActions = actionQueue.getAllEncoded(),
        firstActionId = actionQueue.firstMutatorId(),
    )

    context(tx: WriteTransaction)
    fun applyTableChanges(changes: TableChanges) {
        val table = changes.table
        tx.prepare("INSERT OR REPLACE INTO $table (id, data, owner) VALUES (?, ?, ?)") {
            changes.changes.forEach { rowChange ->
                bindUuid(1, rowChange.row)
                bindBlob(2, rowChange.data)
                bindLong(3, tx.identity)
                step()
                reset()
            }
        }
    }

    context(tx: WriteTransaction)
    internal fun reconcileDiff(updates: SyncResult) {
        rollbackAll()
        actionQueue.clearAcknowledged(updates.lastActionIdApplied)
        updates.changes.forEach { applyTableChanges(it) }
        actionQueue.invokeAllStored()
    }
}
