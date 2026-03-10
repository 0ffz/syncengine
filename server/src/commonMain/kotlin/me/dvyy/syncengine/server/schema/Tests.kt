package me.dvyy.syncengine.server.schema

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.dvyy.sqlite.Identity
import me.dvyy.syncengine.sync.SyncRequest
import me.dvyy.syncengine.sync.SyncResult
import me.dvyy.syncengine.sync.SyncService
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid


class MockAwaitingSyncService(val server: SyncServer, val user: Identity) : SyncService {
    private val requestStart = Channel<Unit>(Channel.RENDEZVOUS)
    private val requestReturn = Channel<Unit>(Channel.RENDEZVOUS)

    override suspend fun sync(uuid: Uuid, initialRequest: SyncRequest, request: Flow<SyncRequest>): Flow<SyncResult> {
        return flowOf(sync(initialRequest))
    }

    suspend fun sync(request: SyncRequest): SyncResult {
        // Wait for signal before proceeding
        requestStart.receive()
        val result = server.sync(request, user)
        requestReturn.receive()
        return result
    }

    suspend fun sendRequest() {
        requestStart.send(Unit)
    }

    suspend fun respond() {
        requestReturn.send(Unit)
    }
}

fun SyncServer.mockAwaitingService(user: Identity) = MockAwaitingSyncService(this, user)

fun SyncServer.mockService(
    user: Identity,
    delay: Duration = 0.seconds,
) = object : SyncService {
    override suspend fun sync(
        uuid: Uuid,
        initialRequest: SyncRequest,
        request: Flow<SyncRequest>,
    ): Flow<SyncResult> = streamingSync(user, initialRequest, request)
//    override suspend fun sync(request: SyncRequest): SyncResult {
//        delay(delay)
//        return sync(request, user)
//    }
}

