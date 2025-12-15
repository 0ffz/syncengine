package me.dvyy.syncengine.actions

/**
 * Represents an action that should be ignored, useful when reducing with previous action has the effect of doing nothing
 */
object IdentityAction : Action {
    override fun reduce(previous: Action): Action? = if (previous == IdentityAction) IdentityAction else null
}