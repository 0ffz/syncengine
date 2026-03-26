package me.dvyy.syncengine.jsonactions.actions

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import me.dvyy.syncengine.actions.Action
import me.dvyy.syncengine.schema.JsonElementAsStringSerializer
import kotlin.uuid.Uuid

@Serializable
data class JsonCreateAction(
    val table: String,
    val id: Uuid,
    val data: @Serializable(with = JsonElementAsStringSerializer::class) JsonElement,
) : Action