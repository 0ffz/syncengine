package me.dvyy.syncengine.schema

interface Mutators<M : AbstractMutator<*>> {
    suspend operator fun invoke(mutator: M)
}
