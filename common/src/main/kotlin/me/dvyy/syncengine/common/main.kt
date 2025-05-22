package me.dvyy.syncengine.common

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import java.util.concurrent.ConcurrentLinkedQueue

@OptIn(ExperimentalSerializationApi::class, FlowPreview::class)
class SyncClient(
    val client: ClientDataStore,
    val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) {
    val httpClient: HttpClient = HttpClient {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(ProtoBuf)
        }
        defaultRequest {
            port = 8080
            host = "127.0.0.1"
        }
    }
    data class Acknowledged(val incomingQueued: Int, val mutatorsAcknowledged: Int)
    val incomingScope = Dispatchers.IO.limitedParallelism(1)
    val incomingChanges = ConcurrentLinkedQueue<SyncResult.Updates>()
    val acknowledgedChangeCount = Channel<Acknowledged>(RENDEZVOUS)

    //    client.reconcileDiff(result)
//    println("[Client] reconciled state is ${client.store}")
    suspend fun receiveIncoming(result: SyncResult) = when(result) {
        is SyncResult.MutatorsAck -> acknowledgedChangeCount.send(Acknowledged(incomingChanges.size, result.amount))
        is SyncResult.Updates -> {
            withContext(incomingScope) {
                if (client.mutatorsCalled.isNotEmpty()) {
                    incomingChanges.add(result)
                    println("[Client] queued $result")
                } else {
                    client.reconcileDiff(result)
                    println("[Client] reconciled state is ${client.store}")
                }
            }
        }
    }

    suspend fun clearMutators(amount: Int) {
        withContext(incomingScope) {
            repeat(amount) { client.mutatorsCalled.remove() }
        }
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            // Constant incoming row changes flow
            httpClient.webSocket("/sync") {
                val job = launch {
                    while(true) {
                        client.store.changes.receive() // await a mutator change
                        val mutators = client.mutatorsCalled.toList()
                        val expectedCount = mutators.size
                        sendSerialized<SyncRequest>(SyncRequest.ApplyMutators(mutators))
                        val ack = acknowledgedChangeCount.receive()
                        //TODO confirm acknowledge count
                        repeat(ack.mutatorsAcknowledged) { client.mutatorsCalled.remove() }
                        client.reconcileDiff(ack.incomingQueued) { incomingChanges.poll() }
                        println("[Client] reconciled state is ${client.store}")
                    }
                }
                incoming.receiveAsFlow().collect { frame ->
                    val result = ProtoBuf.decodeFromByteArray(SyncResult.serializer(), frame.data)
                    receiveIncoming(result)
                }
                job.cancel()
            }
            println("[Client] disconnected")
        }
    }
}

@OptIn(FlowPreview::class)
suspend fun main() {
//    val server = ServerDataStore()
//    val clientServerTransport = ClientServerTransport()
//    val serverClientTransport = ServerClientTransport()
//    val syncService = SyncServiceImpl(Job(), server)


//    while (true) {
//        server.apply(serverClientTransport.receive())
//        server.store.forEach { (row, value) ->
//            serverClientTransport.send(RowDiff(row, value))
//        }
//    }
//    val service: TestService = client.withService<TestService>()
//    val server = Mutators(serverClientTransport)
//    server.registerService<TestService> { ctx -> TestServiceImpl(ctx) }
//    println(service.test(1))
}
