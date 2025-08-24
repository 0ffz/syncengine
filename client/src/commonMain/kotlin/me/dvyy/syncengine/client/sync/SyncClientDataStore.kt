package me.dvyy.syncengine.client.sync

import me.dvyy.sqlite.Transaction
import me.dvyy.sqlite.WriteTransaction
import me.dvyy.syncengine.client.kvstore.KVStore
import me.dvyy.syncengine.client.kvstore.KVStoreProperty
import me.dvyy.syncengine.client.mutators.ClientSchema
import me.dvyy.syncengine.client.mutators.MutatorQueue
import me.dvyy.syncengine.sync.SyncRequest
import me.dvyy.syncengine.sync.SyncResult

class SyncClientDataStore(
    private val schema: ClientSchema,
    private val mutators: MutatorQueue<*, *>,
) {
    val lastFrameSeen = KVStoreProperty("lastFrameSeen", KVStore)

    context(tx: Transaction)
    fun getSyncRequest() = SyncRequest(
        (lastFrameSeen.getString()?.toLong() ?: 0L),
        mutators.getAllEncoded(),
        mutators.firstMutatorId(),
    )

    context(tx: WriteTransaction)
    fun reconcileDiff(updates: SyncResult) {
        schema.rollbackAll()
        mutators.clearAcknowledged(updates.lastMutatorIdApplied)
        mutators.invokeAllStored()
    }
}

