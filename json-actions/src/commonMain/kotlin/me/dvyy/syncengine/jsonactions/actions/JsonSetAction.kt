package me.dvyy.syncengine.jsonactions.actions

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import me.dvyy.syncengine.actions.Action
import me.dvyy.syncengine.schema.JsonElementAsStringSerializer
import kotlin.uuid.Uuid

@Serializable
data class JsonSetAction(
    val table: String,
    val id: Uuid,
    val path: String,
    val patch: @Serializable(with = JsonElementAsStringSerializer::class) JsonElement,
) : Action {
    override fun reduce(previous: Action): Action? {
        return null //TODO
//        return when (previous) {
//            is JsonSetAction if previous.table == table && previous.id == id -> {
//                JsonSetAction(table, id, previous.patch + patch)
//            }
//
//            else -> null
//        }
    }
}