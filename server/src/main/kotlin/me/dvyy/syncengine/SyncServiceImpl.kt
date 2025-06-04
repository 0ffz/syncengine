package me.dvyy.syncengine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import me.dvyy.syncengine.common.RowDiff
import me.dvyy.syncengine.common.SyncRequest
import me.dvyy.syncengine.common.SyncResult
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import kotlin.apply
import kotlin.coroutines.CoroutineContext
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class SyncServiceImpl(
    override val coroutineContext: CoroutineContext,
    val store: ServerDataStore,
) : CoroutineScope {
    val concurrentContext = Dispatchers.IO.limitedParallelism(1)

    @OptIn(ExperimentalTime::class)
    suspend fun sync(
//        lastClientSyncTimestamp: Long,
//        lastMutatorApplied: Int,
        request: SyncRequest.ApplyMutators,
    ) {
        if(request.mutators.isEmpty()) return
        suspendTransaction(db = store.db) {
            val now = System.currentTimeMillis()
            store.apply(request.mutators)
            //TODO track last applied mutator for client to avoid re-applying
//            store.getUpdatedSince(now)
        }
    }
}
