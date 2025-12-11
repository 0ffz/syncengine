package me.dvyy.syncengine.jsonactions.actions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import me.dvyy.syncengine.actions.Action
import kotlin.uuid.Uuid

@Serializable
@SerialName("create")
data class JsonCreateAction(
    val table: String,
    val id: Uuid,
    val data: JsonElement,
) : Action