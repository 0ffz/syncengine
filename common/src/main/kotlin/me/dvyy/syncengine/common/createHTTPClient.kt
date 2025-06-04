package me.dvyy.syncengine.common

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.protobuf.protobuf
import kotlinx.serialization.protobuf.ProtoBuf

fun createHTTPClient() = HttpClient {
//    install(WebSockets.Plugin) {
//        contentConverter = KotlinxWebsocketSerializationConverter(ProtoBuf.Default)
//    }
    defaultRequest {
        port = 8080
        host = "127.0.0.1"
    }
    install(ContentNegotiation) {
        protobuf()
    }
}