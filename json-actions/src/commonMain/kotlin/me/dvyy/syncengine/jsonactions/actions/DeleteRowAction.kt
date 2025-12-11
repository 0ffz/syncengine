package me.dvyy.syncengine.jsonactions.actions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.dvyy.syncengine.actions.Action
import kotlin.uuid.Uuid

@Serializable
@SerialName("delete")
data class DeleteRowAction(
    val table: String,
    val id: Uuid,
) : Action
