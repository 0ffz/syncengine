package me.dvyy.syncengine.jsonactions

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import me.dvyy.sqlite.Database
import me.dvyy.syncengine.actions.Actions
import me.dvyy.syncengine.jsonactions.actions.DeleteRowAction
import me.dvyy.syncengine.jsonactions.actions.JsonCreateAction
import me.dvyy.syncengine.jsonactions.actions.JsonPatchAction
import me.dvyy.syncengine.schema.minus
import kotlin.uuid.Uuid

class JsonActions<T>(
    val db: Database,
    val dao: JsonDataQueries<T>,
    val actions: Actions,
) {
    val jsonNoDefaults = Json { encodeDefaults = false }

    suspend fun create(element: T, uuid: Uuid = Uuid.generateV7()): Uuid = actions.invoke(
        JsonCreateAction(
            dao.table.name,
            uuid,
            dao.json.encodeToJsonElement(dao.serializer, element)
        )
    ).let { uuid }

    suspend fun delete(uuid: Uuid) = actions.invoke(
        DeleteRowAction(
            dao.table.name,
            uuid
        )
    )

    suspend fun patch(uuid: Uuid, element: T) {
        val existing = db.read { dao.getJsonElement(uuid) }
        val new = jsonNoDefaults.encodeToJsonElement(dao.serializer, element)
        val patch = new - existing
        if ((patch as? JsonObject)?.size == 0) return
        actions.invoke(
            JsonPatchAction(
                table = dao.table.name,
                id = uuid,
                patch = patch
            )
        )
    }
}
