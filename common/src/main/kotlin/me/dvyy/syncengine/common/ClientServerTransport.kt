package me.dvyy.syncengine.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.dvyy.syncengine.common.mutators.Mutator
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import kotlin.coroutines.CoroutineContext

class ClientServerTransport {
    val outChannel = Channel<EncodedMutator>()
    val inChannel = Channel<SyncResult>()
    val coroutineContext: CoroutineContext = Job()
    suspend fun send(message: EncodedMutator) {
        outChannel.send(message).also { println("[Client] Sent ${message.contentToString()}") }
    }

    suspend fun receive(): SyncResult {
        return inChannel.receive().also { println("[Client] Received $it") }
    }
}

class ServerClientTransport {
    val outChannel = Channel<SyncResult>()
    val inChannel = Channel<EncodedMutator>()
    val coroutineContext: CoroutineContext = Job()
    suspend fun send(message: SyncResult) {
        outChannel.send(message).also { println("[Server] Sent $message") }
    }

    suspend fun receive(): Mutator {
        return ClientDataStore.decode(inChannel.receive()).also { println("[Server] Received $it") }
    }
}

data class SyncResult(
    val diffs: List<RowDiff>,
    val lastModificationTimestamp: Long,
    val lastMutatorApplied: Int,
)

data class SyncRequest(
    val mutators: List<Mutator>,
    val lastSyncTimestamp: Long,
)

class SyncServiceImpl(
    override val coroutineContext: CoroutineContext,
    val store: ServerDataStore,
) : CoroutineScope {
    var lastClientSyncTimestamp = 0L
    var lastMutatorApplied = 0
    val concurrentContext = Dispatchers.IO.limitedParallelism(1)

    suspend fun sync(request: SyncRequest): SyncResult {
        return suspendTransaction(db = store.db) {
            val drop = if(lastClientSyncTimestamp == request.lastSyncTimestamp) lastMutatorApplied else 0
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
