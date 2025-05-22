package me.dvyy.syncengine.common

import kotlinx.serialization.Serializable
import me.dvyy.syncengine.common.mutators.Mutator

//class ClientServerTransport {
//    val outChannel = Channel<EncodedMutator>()
//    val inChannel = Channel<SyncResult>()
//    val coroutineContext: CoroutineContext = Job()
//    suspend fun send(message: EncodedMutator) {
//        outChannel.send(message).also { println("[Client] Sent ${message.contentToString()}") }
//    }
//
//    suspend fun receive(): SyncResult {
//        return inChannel.receive().also { println("[Client] Received $it") }
//    }
//}
//
//class ServerClientTransport {
//    val outChannel = Channel<SyncResult>()
//    val inChannel = Channel<EncodedMutator>()
//    val coroutineContext: CoroutineContext = Job()
//    suspend fun send(message: SyncResult) {
//        outChannel.send(message).also { println("[Server] Sent $message") }
//    }
//
//    suspend fun receive(): Mutator {
//        return ClientDataStore.decode(inChannel.receive()).also { println("[Server] Received $it") }
//    }
//}

//@Serializable
//data class SyncResult(
//    val diffs: List<RowDiff>,
//    val lastModificationTimestamp: Long,
//    val mutatorsAcknowledged: Int,
//)

//@Serializable
//data class SyncRequest(
//    val mutators: List<Mutator>,
//    val lastSyncTimestamp: Long,
//)

@Serializable
sealed interface SyncRequest {
    @Serializable
    class ChangesSince(val timestamp: Long) : SyncRequest

    @Serializable
    class ApplyMutators(val mutators: List<Mutator>) : SyncRequest
}
