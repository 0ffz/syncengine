package me.dvyy.syncengine.common.sync

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.ExperimentalSerializationApi
import me.dvyy.syncengine.common.ClientDataStore
import me.dvyy.syncengine.common.createHTTPClient
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalSerializationApi::class, FlowPreview::class)
class SyncClient(
    val client: ClientDataStore,
    val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) {
    val httpClient: HttpClient = createHTTPClient()

    val incomingScope = Dispatchers.IO.limitedParallelism(1)
    val incomingChanges = ConcurrentLinkedQueue<SyncResult.Updates>()
    val acknowledgedChangeCount = Channel<Acknowledged>(Channel.Factory.RENDEZVOUS)

    //    client.reconcileDiff(result)
//    println("[Client] reconciled state is ${client.store}")
//    suspend fun receiveIncoming(result: SyncResult) = when (result) {
//        is SyncResult.MutatorsAck -> acknowledgedChangeCount.send(Acknowledged(incomingChanges.size, result.amount))
//        is SyncResult.Updates -> {
//            withContext(incomingScope) {
//                if (client.mutatorsCalled.isNotEmpty()) {
//                    incomingChanges.add(result)
//                    println("[Client] queued $result")
//                } else {
//                    client.reconcileDiff(result)
//                    println("[Client] reconciled state is ${client.store}")
//                }
//            }
//        }
//    }

    suspend fun sync() {
        val mutators = transaction { client.mutatorQueue.getMutatorsToSend() }
        val updates = httpClient.post("/sync") {
            contentType(ContentType.Application.ProtoBuf)
            setBody(SyncRequest.ApplyMutators(mutators, client.lastSyncTimestamp))
        }.body<SyncResult.Updates>()
        transaction {
            client.mutatorQueue.clearMutators(mutators.size)
            client.reconcileDiff(updates)
        }
//
//                    val expectedCount = mutators.size
//                    sendSerialized<SyncRequest>(SyncRequest.ApplyMutators(mutators))
//                    val ack = acknowledgedChangeCount.receive()
//                    //TODO confirm acknowledge count
//                    repeat(ack.mutatorsAcknowledged) { client.mutatorsCalled.remove() }
//                    client.reconcileDiff(ack.incomingQueued) { incomingChanges.poll() }
//                    println("[Client] reconciled state is ${client.store}")
//                }
//            }
//            incoming.receiveAsFlow().collect { frame ->
//                val result = ProtoBuf.Default.decodeFromByteArray(SyncResult.serializer(), frame.data)
//                receiveIncoming(result)
//            }
//            job.cancel()
//            println("[Client] disconnected")
//        }
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
//                client.store.changes.receive()
                println("Sending changes...")
                sync()
                println("Sync complete, waiting for changes")
                delay(1.seconds)
            }
        }
//            // Constant incoming row changes flow
////            httpClient.webSocket("/sync") {
////                val job = launch {
////                    while (true) {
////                        client.store.changes.receive() // await a mutator change
////                        val mutators = client.mutatorsCalled.toList()
////                        val expectedCount = mutators.size
////                        sendSerialized<SyncRequest>(SyncRequest.ApplyMutators(mutators))
////                        val ack = acknowledgedChangeCount.receive()
////                        //TODO confirm acknowledge count
////                        repeat(ack.mutatorsAcknowledged) { client.mutatorsCalled.remove() }
////                        client.reconcileDiff(ack.incomingQueued) { incomingChanges.poll() }
////                        println("[Client] reconciled state is ${client.store}")
////                    }
////                }
////                incoming.receiveAsFlow().collect { frame ->
////                    val result = ProtoBuf.Default.decodeFromByteArray(SyncResult.serializer(), frame.data)
////                    receiveIncoming(result)
////                }
////                job.cancel()
////            }
////            println("[Client] disconnected")
//        }
    }
}
