package me.dvyy.syncengine.common

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.chunked
import kotlinx.coroutines.flow.receiveAsFlow

@OptIn(FlowPreview::class)
suspend fun main() {
    val client = ClientDataStore(ReversibleDataStore())
    val server = ServerDataStore()
    val clientServerTransport = ClientServerTransport()
    val serverClientTransport = ServerClientTransport()
    val syncService = SyncServiceImpl(Job(), server)

    CoroutineScope(Dispatchers.IO).launch {
        launch {
            repeat(100) {
                client.test()
                clientServerTransport.send(client.encodedMutators.last())
                delay(100)
            }
        }
//        launch {
//            while(true) {
//
//            }
//        }
        launch {
            clientServerTransport.outChannel.receiveAsFlow().chunked(3).collect {
                val result = syncService.sync(
                    SyncRequest(
                        client.mutatorsCalled.toList(),
                        client.lastSyncTimestamp
                    )
                )
                delay(500)
                clientServerTransport.inChannel.send(result)
//                serverClientTransport.inChannel.send(it)
            }
        }
        launch {
            serverClientTransport.outChannel.receiveAsFlow().collect {
                delay(500)
//
//                clientServerTransport.inChannel.send(it)
            }
        }
        launch {
            clientServerTransport.inChannel.receiveAsFlow().collect {
                println("[Client] received: $it")
                client.reconcileDiff(it)
                println("[Client] reconciled state is ${client.store}")
            }
        }
    }.join()

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
