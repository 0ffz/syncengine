package me.dvyy.syncengine.sync

interface SyncService {
    suspend fun sync(request: SyncRequest): SyncResult
}