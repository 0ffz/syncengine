package me.dvyy.syncengine.client.sync

import io.ktor.client.*
import io.ktor.client.plugins.*

internal fun createHTTPClient() = HttpClient {
//    install(WebSockets.Plugin) {
//        contentConverter = KotlinxWebsocketSerializationConverter(ProtoBuf.Default)
//    }
    defaultRequest {
        port = 8080
        host = "127.0.0.1"
    }
//        install(ContentNegotiation) {
//            protobuf()
//        }
}
