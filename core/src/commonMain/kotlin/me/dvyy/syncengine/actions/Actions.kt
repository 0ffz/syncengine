package me.dvyy.syncengine.actions

import kotlinx.coroutines.Job

interface Actions {
    suspend operator fun invoke(action: Action)

    suspend operator fun invoke(actions: Collection<Action>)

    suspend operator fun invoke(vararg actions: Action) = invoke(actions.toList())

    fun invokeAsync(action: Action): Job

    fun invokeAsync(actions: Collection<Action>): Job

    fun invokeAsync(vararg actions: Action) = invokeAsync(actions.toList())
}