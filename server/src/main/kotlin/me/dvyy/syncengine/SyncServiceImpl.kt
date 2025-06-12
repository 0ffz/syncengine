package me.dvyy.syncengine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import me.dvyy.syncengine.common.SyncRequest
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.coroutines.CoroutineContext
import kotlin.time.ExperimentalTime

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
        if (request.mutators.isEmpty()) return
        transaction(db = store.db) {
            store.apply(request.mutators)
            //TODO track last applied mutator for client to avoid re-applying
//            store.getUpdatedSince(now)
        }
    }
}
