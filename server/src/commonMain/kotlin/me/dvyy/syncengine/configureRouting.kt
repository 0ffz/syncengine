package me.dvyy.syncengine

import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun Application.configureRouting() {
//    val store = SyncServerDataStore()
//    val service = SyncServiceImpl(Dispatchers.IO, store)
//    val updateStream = MutableStateFlow<SyncResult.Updates?>(null)
    routing {
        post("/sync") {
            // get SyncRequest
//            val request = call.receive<SyncRequest.ApplyMutators>()
//            service.sync(request)
//            call.respond<SyncResult.Updates>(
//                store.getUpdatedSince(request.lastSync),
//            )
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

