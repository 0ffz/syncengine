package me.dvyy.syncengine.jsonactions.actions

import kotlinx.serialization.Serializable
import me.dvyy.syncengine.actions.Action
import kotlin.uuid.Uuid

@Serializable
data class DeleteEntityAction(
    val id: Uuid,
) : Action {
    override fun reduce(previous: Action): Action? = when (previous) {
        is JsonCreateAction if previous.id == id -> Action.IDENTITY
        else -> null
    }
}
