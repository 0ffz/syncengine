package me.dvyy.syncengine

//fun Routing.syncWebsocket() {
//    val store = SyncServerDataStore()
//    val service = SyncServiceImpl(Dispatchers.IO, store)
//    val updateStream = MutableStateFlow<SyncResult.Updates?>(null)
//
//    webSocket("/sync") {
//        val job = launch {
//            updateStream.collect { updates ->
//                if (updates == null) return@collect
//                println("[Server] forwarding changes to client")
//                sendSerialized<SyncResult>(updates)
//            }
//        }
//        try {
//            incoming.consumeAsFlow().collect { frame ->
//                val request = ProtoBuf.Default.decodeFromByteArray(SyncRequest.serializer(), frame.data)
//                println("Received request: $request")
////                when (request) {
////                    is SyncRequest.ChangesSince -> {
////                        sendSerialized<SyncResult>(store.getUpdatedSince(request.timestamp))
////                    }
////
////                    is SyncRequest.ApplyMutators -> {
////                        updateStream.emit(service.sync(request))
////                        sendSerialized<SyncResult>(SyncResult.MutatorsAck(request.mutators.size))
////                    }
////                }
//            }
//        } finally {
//            job.cancel()
//            println("[Server] disconnected")
//        }
//    }
//}
