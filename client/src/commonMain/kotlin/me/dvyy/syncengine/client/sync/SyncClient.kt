package me.dvyy.syncengine.client.sync

import me.dvyy.sqlite.Database
import me.dvyy.sqlite.Transaction
import me.dvyy.sqlite.WriteTransaction
import me.dvyy.syncengine.client.kvstore.KVStore
import me.dvyy.syncengine.client.kvstore.KVStoreProperty
import me.dvyy.syncengine.client.mutators.MutatorQueue
import me.dvyy.syncengine.client.mutators.MutatorsTable
import me.dvyy.syncengine.client.mutators.RollbackJsonTable
import me.dvyy.syncengine.schema.Schema
import me.dvyy.syncengine.sync.SyncRequest
import me.dvyy.syncengine.sync.SyncResult
import me.dvyy.syncengine.sync.SyncService

/**
 * Entrypoint for client syncengine.
 */
class SyncClient(
    private val db: Database,
    private val mutators: MutatorQueue<*, *>,
    private val schema: Schema,
    private val syncService: SyncService,
) {
    val syncedTables = schema.syncedTables.map { RollbackJsonTable(it) }
    val views = schema.views
    val lastFrameSeen = KVStoreProperty("lastFrameSeen", KVStore)

    suspend fun initialize() = db.write {
        MutatorsTable.create()
        KVStore.create()
        syncedTables.forEach { it.create() }
        views.forEach { it.create() }
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


    context(tx: Transaction)
    internal fun getSyncRequest() = SyncRequest(
        (lastFrameSeen.getString()?.toLong() ?: 0L),
        mutators.getAllEncoded(),
        mutators.firstMutatorId(),
    )

    context(tx: WriteTransaction)
    internal fun reconcileDiff(updates: SyncResult) {
        rollbackAll()
        mutators.clearAcknowledged(updates.lastMutatorIdApplied)
        mutators.invokeAllStored()
    }
}
