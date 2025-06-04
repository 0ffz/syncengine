package me.dvyy.syncengine

import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.protobuf.protobuf
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.websocket.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalSerializationApi::class)
fun main() {
    startSyncServer()
}


fun startSyncServer() {
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            protobuf()
        }
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