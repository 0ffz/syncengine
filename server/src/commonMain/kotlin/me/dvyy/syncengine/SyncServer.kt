package me.dvyy.syncengine

import io.ktor.server.engine.*
import io.ktor.server.netty.*

class SyncServer {
    val server = embeddedServer(Netty, port = 8080) {
//        install(ContentNegotiation) {
//            protobuf()
//        }
//        install(WebSockets) {
//            contentConverter = KotlinxWebsocketSerializationConverter(ProtoBuf)
//            pingPeriod = 15.seconds
//            timeout = 15.seconds
//            maxFrameSize = Long.MAX_VALUE
//            masking = false
//        }
        configureRouting()
    }.start(wait = true)
}