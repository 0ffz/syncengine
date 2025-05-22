package me.dvyy.syncengine

import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import me.dvyy.syncengine.common.SyncRequest
import me.dvyy.syncengine.common.SyncResult
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalSerializationApi::class)
fun main() {
    embeddedServer(Netty, port = 8080) {
        val store = ServerDataStore()
        val service = SyncServiceImpl(Dispatchers.IO, store)
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(ProtoBuf)
            pingPeriod = 15.seconds
            timeout = 15.seconds
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }

        val updateStream = MutableStateFlow<SyncResult.Updates?>(null)
        routing {
            webSocket("/sync") {
                val job = launch {
                    updateStream.collect { updates ->
                        if(updates == null) return@collect
                        println("[Server] forwarding changes to client")
                        sendSerialized<SyncResult>(updates)
                    }
                }
                try {
                    incoming.consumeAsFlow().collect { frame ->
                        val request = ProtoBuf.decodeFromByteArray(SyncRequest.serializer(), frame.data)
                        println("Received request: $request")
                        when (request) {
                            is SyncRequest.ChangesSince -> {
                                sendSerialized<SyncResult>(store.getUpdatedSince(request.timestamp))
                            }

                            is SyncRequest.ApplyMutators -> {
                                updateStream.emit(service.sync(request))
                                sendSerialized<SyncResult>(SyncResult.MutatorsAck(request.mutators.size))
                            }
                        }
                    }
                } finally {
                    job.cancel()
                    println("[Server] disconnected")
                }
            }
        }
    }.start(wait = true)
}
