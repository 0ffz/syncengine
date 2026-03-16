@file:OptIn(ExperimentalTime::class)

package me.dvyy.syncengine.server.schema

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import me.dvyy.sqlite.Database
import me.dvyy.sqlite.Identity
import me.dvyy.syncengine.sync.SyncRequest
import me.dvyy.syncengine.sync.SyncResult
import kotlin.time.ExperimentalTime

class SyncServer(
    private val rootDb: Database,
    private val workspaces: WorkspaceRepository,
) {
    fun streamingSync(
        user: Identity,
        initialRequest: SyncRequest,
        request: Flow<SyncRequest>,
    ): Flow<SyncResult> = flow {
        workspaces.use(user) { workspace ->
            emitAll(workspace.streamingSync(user, initialRequest, request))
        }
    }

    suspend fun initialize() = rootDb.write {
        ServerQueries().create()
        workspaces.workspaces.table.create()
    }


    companion object {
        fun of(
        ) {


        }
    }
}
