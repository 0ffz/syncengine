package me.dvyy.syncengine

import io.ktor.server.routing.Routing
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.protobuf.ProtoBuf
import me.dvyy.syncengine.common.sync.SyncRequest
import me.dvyy.syncengine.common.sync.SyncResult

fun Routing.syncWebsocket() {
    val store = ServerDataStore()
    val service = SyncServiceImpl(Dispatchers.IO, store)
    val updateStream = MutableStateFlow<SyncResult.Updates?>(null)

    webSocket("/sync") {
        val job = launch {
            updateStream.collect { updates ->
                if (updates == null) return@collect
                println("[Server] forwarding changes to client")
                sendSerialized<SyncResult>(updates)
            }
        }
        try {
            incoming.consumeAsFlow().collect { frame ->
                val request = ProtoBuf.Default.decodeFromByteArray(SyncRequest.serializer(), frame.data)
                println("Received request: $request")
//                when (request) {
//                    is SyncRequest.ChangesSince -> {
//                        sendSerialized<SyncResult>(store.getUpdatedSince(request.timestamp))
//                    }
//
//                    is SyncRequest.ApplyMutators -> {
//                        updateStream.emit(service.sync(request))
//                        sendSerialized<SyncResult>(SyncResult.MutatorsAck(request.mutators.size))
//                    }
//                }
            }
        } finally {
            job.cancel()
            println("[Server] disconnected")
        }
    }
}
