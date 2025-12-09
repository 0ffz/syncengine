package me.dvyy.syncengine.client.sync

import me.dvyy.sqlite.Database
import me.dvyy.sqlite.Transaction
import me.dvyy.sqlite.WriteTransaction
import me.dvyy.syncengine.client.kvstore.KVStoreProperty
import me.dvyy.syncengine.client.mutators.ActionQueue
import me.dvyy.syncengine.client.mutators.RollbackJsonTable
import me.dvyy.syncengine.client.schema.ClientDatabase
import me.dvyy.syncengine.schema.Schema
import me.dvyy.syncengine.sync.SyncRequest
import me.dvyy.syncengine.sync.SyncResult
import me.dvyy.syncengine.sync.SyncService
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Entrypoint for client syncengine.
 */
@OptIn(ExperimentalUuidApi::class)
class SyncClient(
    private val db: Database,
    private val mutators: ActionQueue,
    private val schema: Schema,
    private val syncService: SyncService,
) {
    private val clientDAO = ClientDatabase()
    val syncedTables = schema.syncedTables.map { RollbackJsonTable(it) }
    val views = schema.views
    val lastFrameSeen = KVStoreProperty("lastFrameSeen")
    val deviceId = KVStoreProperty("deviceId")

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
        encodedActions = mutators.getAllEncoded(),
        firstActionId = mutators.firstMutatorId(),
    )

    context(tx: WriteTransaction)
    internal fun reconcileDiff(updates: SyncResult) {
        rollbackAll()
        mutators.clearAcknowledged(updates.lastActionIdApplied)
        mutators.invokeAllStored()
    }
}
