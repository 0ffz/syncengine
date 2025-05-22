package me.dvyy.syncengine.common

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import kotlinx.coroutines.*
import kotlinx.serialization.protobuf.ProtoBuf

class SyncClient(
    val client: ClientDataStore,
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

    init {
        CoroutineScope(Dispatchers.IO).launch {
            httpClient.webSocket("/sync") {
                launch {
                    while (true) {
                        if (client.mutatorsCalled.isNotEmpty()) sendSerialized(
                            SyncRequest(
                                client.mutatorsCalled.toList(),
                                client.lastSyncTimestamp
                            )
                        )
                        delay(1000)
                    }
                }
                while (true) {
                    val result = receiveDeserialized<SyncResult>()
                    println("[Client] received: $result")
                    client.reconcileDiff(result)
                    println("[Client] reconciled state is ${client.store}")
                }
            }
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
