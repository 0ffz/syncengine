package me.dvyy.syncengine.server.schema

import me.dvyy.sqlite.Database
import me.dvyy.sqlite.Identity
import me.dvyy.syncengine.jsonactions.JsonDataQueries
import me.dvyy.syncengine.schema.jsonTable
import kotlin.uuid.Uuid

class WorkspaceRepository(
    val rootDb: Database,
    val stopTimeoutMillis: Long,
) {
    val dbPool: ItemPool<Workspace> = ItemPool(initialize = {
        Workspace(Database("users/${it.toHexDashString()}"), TODO(), TODO(), TODO()).also { it.initialize() }
    }, onClose = { it.close() }, stopTimeoutMillis = stopTimeoutMillis)

    val workspaces = JsonDataQueries(WorkspaceModel.serializer(), jsonTable("workspaces"))

    suspend inline fun <T> use(identity: Identity, crossinline use: suspend (Workspace) -> T): T {
        val workspaceUuid = rootDb.read { workspaces.query("data -> 'owner' = ?", identity)?.first }
            ?: rootDb.write { workspaces.create(Uuid.random(), WorkspaceModel(name = "Unnamed", owner = identity)) }
        return dbPool.use(workspaceUuid, use)
    }
}