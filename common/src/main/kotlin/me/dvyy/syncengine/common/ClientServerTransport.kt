package me.dvyy.syncengine.common

import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import me.dvyy.syncengine.common.mutators.Mutator
import kotlin.coroutines.CoroutineContext

class ClientServerTransport {
    val outChannel = Channel<EncodedMutator>()
    val inChannel = Channel<RowDiff>()
    val coroutineContext: CoroutineContext = Job()
    suspend fun send(message: EncodedMutator) {
        outChannel.send(message).also { println("[Client] Sent ${message.contentToString()}") }
    }

    suspend fun receive(): RowDiff {
        return inChannel.receive().also { println("[Client] Received $it") }
    }
}

class ServerClientTransport {
    val outChannel = Channel<RowDiff>()
    val inChannel = Channel<EncodedMutator>()
    val coroutineContext: CoroutineContext = Job()
    suspend fun send(message: RowDiff) {
        outChannel.send(message).also { println("[Server] Sent $message") }
    }

    suspend fun receive(): Mutator {
        return MutatorDataStore.decode(inChannel.receive()).also { println("[Server] Received $it") }
    }
}
