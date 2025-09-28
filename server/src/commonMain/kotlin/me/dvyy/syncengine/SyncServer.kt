@file:OptIn(ExperimentalTime::class)

package me.dvyy.syncengine

import me.dvyy.sqlite.Database
import me.dvyy.sqlite.Identity
import me.dvyy.sqlite.tables.View
import me.dvyy.syncengine.schema.Schema
import me.dvyy.syncengine.schema.UserRestrictedJsonTable
import me.dvyy.syncengine.sync.SyncRequest
import me.dvyy.syncengine.sync.SyncResult
import me.dvyy.syncengine.sync.SyncService
import kotlin.time.ExperimentalTime

class SyncServer(
    private val db: Database,
    private val schema: Schema,
) {
    val syncedTables: List<UserRestrictedJsonTable> = schema.syncedTables.map { UserRestrictedJsonTable(it) }
    val views: List<View> = schema.views

    suspend fun sync(
        request: SyncRequest,
        identity: Identity,
    ): SyncResult {
        TODO()
    }

    internal suspend fun initialize() = db.write {
        syncedTables.forEach { it.create() }
        views.forEach { it.create() }
    }
}

fun SyncServer.mockService(user: Identity) = object : SyncService {
    override suspend fun sync(request: SyncRequest): SyncResult {
        return sync(request, user)
    }
}