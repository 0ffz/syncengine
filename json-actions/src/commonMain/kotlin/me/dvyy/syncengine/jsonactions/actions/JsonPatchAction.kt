package me.dvyy.syncengine.jsonactions.actions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import me.dvyy.syncengine.actions.Action
import me.dvyy.syncengine.schema.plus
import kotlin.uuid.Uuid

@Serializable
@SerialName("patch")
data class JsonPatchAction(
    val table: String,
    val id: Uuid,
    val patch: JsonElement,
) : Action {
    override fun reduce(previous: Action): Action? {
        return when (previous) {
            is JsonPatchAction if previous.table == table && previous.id == id -> {
                JsonPatchAction(table, id, previous.patch + patch)
            }

            else -> null
        }
    }
}