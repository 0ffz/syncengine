package me.dvyy.syncengine.sync

import kotlinx.coroutines.flow.Flow
import kotlin.uuid.Uuid

interface SyncService {
    suspend fun sync(
        uuid: Uuid,
        initialRequest: SyncRequest,
        request: Flow<SyncRequest>,
    ): Flow<SyncResult>
}