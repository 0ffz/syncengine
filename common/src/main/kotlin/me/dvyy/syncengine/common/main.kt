package me.dvyy.syncengine.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

suspend fun main() {
    val client = MutatorDataStore(ReversibleDataStore())
    val server = ServerDataStore()
    val clientServerTransport = ClientServerTransport()
    val serverClientTransport = ServerClientTransport()

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
            clientServerTransport.outChannel.receiveAsFlow().collect {
                delay(500); serverClientTransport.inChannel.send(it)
            }
        }
        launch {
            serverClientTransport.outChannel.receiveAsFlow().collect {
                delay(500); clientServerTransport.inChannel.send(it)
            }
        }
        launch {
            clientServerTransport.inChannel.receiveAsFlow().collect {
                println("[Client] received: ${it.row}=\"${it.value}\"")
                println("[Client] state is ${client.store}")
                client.reconcileDiff(arrayOf(it))
            }
        }
    }

    while (true) {
        server.apply(serverClientTransport.receive())
        server.store.forEach { (row, value) ->
            serverClientTransport.send(RowDiff(row, value))
        }
    }
//    val service: TestService = client.withService<TestService>()
//    val server = Mutators(serverClientTransport)
//    server.registerService<TestService> { ctx -> TestServiceImpl(ctx) }
//    println(service.test(1))
}
