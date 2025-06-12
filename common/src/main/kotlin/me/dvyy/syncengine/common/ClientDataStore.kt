package me.dvyy.syncengine.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.ExperimentalSerializationApi
import me.dvyy.syncengine.common.mutators.MutatorQueue
import me.dvyy.syncengine.common.mutators.MutatorsTable
import me.dvyy.syncengine.common.ui.launchTransaction
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.deleteWhere

typealias EncodedMutator = ByteArray

@OptIn(ExperimentalSerializationApi::class)
class ClientDataStore(
    val store: DiffableTables,
    val mutatorQueue: MutatorQueue,
    val scope: CoroutineScope,
) {
    var lastSyncTimestamp = 0L

    //TODO withTransactionContext instead of launch
//    suspend fun reconcileDiff(count: Int, getUpdates: () -> SyncResult.Updates) = launchTransaction {
//        store.rollback()
//        var last: SyncResult.Updates? = null
//        repeat(count) {
//            last = getUpdates()
//            store.setUnderlying(last.updates)
//        }
//        last?.let { lastSyncTimestamp = it.lastChange }
//        mutatorQueue.reconcileStored()
//    }.await()

    fun reconcileDiff(updates: SyncResult.Updates) {
        store.rollback()
        store.setUnderlying(updates.updates)
        lastSyncTimestamp = updates.lastChange
        mutatorQueue.reconcileStored()
    }
}
