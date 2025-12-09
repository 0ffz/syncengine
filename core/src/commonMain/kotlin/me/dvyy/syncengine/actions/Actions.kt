package me.dvyy.syncengine.actions

interface Actions {
    suspend operator fun invoke(mutator: Action)
}