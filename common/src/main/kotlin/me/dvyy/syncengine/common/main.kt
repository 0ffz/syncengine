package me.dvyy.syncengine.common

import kotlinx.coroutines.*

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
