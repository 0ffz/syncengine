//import io.ktor.client.*
//import io.ktor.http.*
//import io.ktor.server.application.*
//import io.ktor.server.engine.*
//import io.ktor.server.netty.*
//import io.ktor.server.routing.*
//import kotlinx.coroutines.channels.Channel
//import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
//import kotlinx.coroutines.coroutineScope
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.flow
//import kotlinx.coroutines.launch
//import kotlinx.rpc.krpc.ktor.client.installKrpc
//import kotlinx.rpc.krpc.ktor.client.rpc
//import kotlinx.rpc.krpc.ktor.client.rpcConfig
//import kotlinx.rpc.krpc.ktor.server.Krpc
//import kotlinx.rpc.krpc.ktor.server.rpc
//import kotlinx.rpc.krpc.serialization.json.json
//import kotlinx.rpc.withService
//import org.jetbrains.exposed.dao.EntityHook
//import org.jetbrains.exposed.dao.id.CompositeID
//import org.jetbrains.exposed.dao.toEntity
//import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
//import kotlin.coroutines.CoroutineContext
//
//suspend fun main() {
//    println("Starting server")
//    embeddedServer(Netty, port = 8080) {
//        install(Krpc)
//
//        routing {
//            rpc("/test") {
//                rpcConfig {
//                    serialization {
//                        json()
//                    }
//                }
//
//                registerService<SyncService> { ctx -> SyncServiceImpl(ctx) }
//
//            }
//        }
//    }.start()
//
//    println("Starting client")
//    val ktorClient = HttpClient {
//        installKrpc {
//            waitForServices = true
//        }
//    }
//    val rpcClient = ktorClient.rpc {
//        url {
//            host = "localhost"
//            port = 8080
//            encodedPath = "test"
//        }
//
//        rpcConfig {
//            serialization {
//                json()
//            }
//        }
//    }
//    val service = rpcClient.withService<SyncService>()
//    coroutineScope {
//        launch {
//            service.sendStream(flow {
//                repeat(10) {
//                    emit(it)
//                    delay(1000)
//                }
//            }).collect { println("Received: $it") }
//            println("Done collecting")
//        }.join()
//    }
//    ktorClient.close()
//    println("Done")
//}
//
//class SyncServiceImpl(
//    val storeSyncedChanges: Boolean,
//    override val coroutineContext: CoroutineContext,
//) : SyncService {
//    /**
//     *
//     */
//    fun applyChange(change: Change<*>) {
//        val existing = OperationDAO.findById(CompositeID {
//            it[OperationsTable.entity] = change.entity
//            it[OperationsTable.componentId] = change.componentId
//        })
//        val accepted = existing == null || change.timestamp > existing.timestamp
//        // Override local modification
//        if(accepted) {
//            if(!storeSyncedChanges) existing?.delete()
//            else {
//                val inserted = existing ?: OperationDAO.new { TODO() }
//                inserted.timestamp = change.timestamp
//                inserted.type = change.type
//            }
//            //TODO insert data
//            change.data
//        }
//    }
//
//    fun applyChangelist(changelist: Changelist) {
//
//    }
//
//    suspend fun processIncoming(incoming: Flow<Changelist>) {
//        incoming.collect {
//            newSuspendedTransaction {
//                applyChangelist(it)
//            }
//        }
//    }
//    override fun synchronize(
//        since: Long,
//        incoming: Flow<Changelist>
//    ): Flow<Changelist> {
//        launch {
//            processIncoming(incoming)
//        }
//
//        return flow {
//            val sinceLastSync = newSuspendedTransaction {
//                OperationDAO
//                    .find { OperationsTable.updateTime greaterEq since }
//                    .map { it }
//            }
//            emit(sinceLastSync)
//
//            val channel = Channel<OperationDAO>(capacity = UNLIMITED)
//            val listener = EntityHook.subscribe {
//                val modification = it.toEntity(OperationDAO) ?: return@subscribe
//                channel.trySend(modification)
//            }
//            try {
//                for(update in channel) {
//                    update
//                    this.emit(Changelist())
//                }
//            } finally {
//                EntityHook.unsubscribe(listener)
//            }
//            // TODO register listener for modifications, stream them back
//        }
//    }
////
////    override fun sendStream(stream: Flow<Int>): Flow<String> {
////        return stream.map {
////            if (it >= 2) cancel()
////            "Replying to: $it"
////        }
////    }
//
////    return channelFlow {
////        send("Initial message from server")
////        stream.collect {
////            send("Replying to: $it")
////            if (it >= 2) this.close()
//////                coroutineContext.cancel(CancellationException("Cancelled by server"))
////            "Replying to: $it"
////        }
////    }
//}
