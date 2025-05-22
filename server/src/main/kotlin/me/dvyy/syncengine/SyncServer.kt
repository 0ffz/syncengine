package me.dvyy.syncengine

import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.protobuf.ProtoBuf
import me.dvyy.syncengine.common.SyncRequest
import me.dvyy.syncengine.common.SyncResult
import kotlin.time.Duration.Companion.seconds

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

//        val sharedFlow = MutableSharedFlow<SyncResult>()
        val channel = MutableStateFlow<SyncResult?>(null)
        routing {
            webSocket("/sync") {
                println("Client connected")
                launch {
                    var lastClientSyncTimestamp = 0L
                    channel.collect {
                        if (lastClientSyncTimestamp == it?.lastModificationTimestamp) return@collect
                        println("[Server] forwarding changes to client")
                        val result = service.sync(SyncRequest(listOf(), lastClientSyncTimestamp))
                        lastClientSyncTimestamp = result.lastModificationTimestamp
                        sendSerialized(result)
                    }
                }
                try {
                    while (true) {
                        val request = receiveDeserialized<SyncRequest>()
                        println("Received request: $request")
                        val result = service.sync(request)
                        channel.emit(result)
                        sendSerialized(result)
                    }
                } finally {
                    println("Client disconnected")
                }
            }
        }
    }.start(wait = true)
}
