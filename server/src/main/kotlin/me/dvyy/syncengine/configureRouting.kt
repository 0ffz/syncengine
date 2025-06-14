package me.dvyy.syncengine

import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import me.dvyy.syncengine.common.SyncRequest
import me.dvyy.syncengine.common.SyncResult
import org.jetbrains.exposed.v1.core.exposedLogger
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun Application.configureRouting() {
    val store = ServerDataStore()
    val service = SyncServiceImpl(Dispatchers.IO, store)
    val updateStream = MutableStateFlow<SyncResult.Updates?>(null)
    routing {
        post("/sync") {
            // get SyncRequest
            val request = call.receive<SyncRequest.ApplyMutators>()
            service.sync(request)
            call.respond<SyncResult.Updates>(
                store.getUpdatedSince(request.lastSync),
            )
//            when (request) {
//                is SyncRequest.ChangesSince -> {
//                    call.respond<SyncResult>(store.getUpdatedSince(request.timestamp))
//                }
//
//                is SyncRequest.ApplyMutators -> {
////                    updateStream.emit(service.sync(request))
////                        SyncResult.MutatorsAck(request.mutators.size)
//                }
        }
    }
}

