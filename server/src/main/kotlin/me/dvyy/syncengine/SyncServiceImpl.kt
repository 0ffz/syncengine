package me.dvyy.syncengine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import me.dvyy.syncengine.common.SyncRequest
import me.dvyy.syncengine.common.SyncResult
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import kotlin.apply
import kotlin.coroutines.CoroutineContext

class SyncServiceImpl(
    override val coroutineContext: CoroutineContext,
    val store: ServerDataStore,
) : CoroutineScope {
    var lastClientSyncTimestamp = 0L
    var lastMutatorApplied = 0
    val concurrentContext = Dispatchers.IO.limitedParallelism(1)

    suspend fun sync(request: SyncRequest): SyncResult {
        return suspendTransaction(db = store.db) {
            val drop = if (lastClientSyncTimestamp == request.lastSyncTimestamp) lastMutatorApplied else 0
            store.apply(request.mutators.drop(drop))
            val updates = store.getUpdatedSince(request.lastSyncTimestamp)
            lastMutatorApplied = request.mutators.size
            SyncResult(
                updates.updates,
                updates.lastTimestamp,
                lastMutatorApplied
            )
        }
    }
}
