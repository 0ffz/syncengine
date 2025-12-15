package me.dvyy.syncengine.actions

interface Action {
    /**
     * Combines this action with another action that occurred right before this one.
     * The result of running reducers on both actions should be the same as the combined action returned here.
     *
     * Returns null if the two cannot be combined.
     */
    fun reduce(previous: Action): Action? = null

    companion object {
        val IDENTITY = IdentityAction
    }
}
