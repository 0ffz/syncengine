package me.dvyy.syncengine.server.schema

import co.touchlab.kermit.Logger
import me.dvyy.sqlite.Database
import me.dvyy.sqlite.Identity
import me.dvyy.syncengine.jsonactions.JsonDataQueries
import me.dvyy.syncengine.reducers.Reducers
import me.dvyy.syncengine.schema.Schema
import me.dvyy.syncengine.schema.jsonTable
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.uuid.Uuid

class WorkspaceRepository(
    val rootDb: Database,
    val workspacesFolder: Path,
    val stopTimeoutMillis: Long,
    val schema: Schema,
    private val reducers: Reducers,
    private val logger: Logger,
) {
    init {
        workspacesFolder.createDirectories()
    }
    val dbPool: ItemPool<Workspace> = ItemPool(initialize = {
        Workspace.of(
            db = Database(
                "${workspacesFolder.absolutePathString()}/${it.toHexDashString()}.db"
            ),
            schema = schema,
            reducers = reducers,
            logger = logger
        ).also { it.initialize() }
    }, onClose = { it.close() }, stopTimeoutMillis = stopTimeoutMillis)

    val workspaces = JsonDataQueries(WorkspaceModel.serializer(), UserRestrictedJsonTable(jsonTable("workspaces")))

    suspend inline fun <T> use(identity: Identity, crossinline use: suspend (Workspace) -> T): T {
        val workspaceUuid = rootDb.read { workspaces.query("data -> 'owner' = ?", identity)?.first }
            ?: rootDb.write { workspaces.create(Uuid.random(), WorkspaceModel(name = "Unnamed", owner = identity)) }
        return dbPool.use(workspaceUuid, use)
    }
}