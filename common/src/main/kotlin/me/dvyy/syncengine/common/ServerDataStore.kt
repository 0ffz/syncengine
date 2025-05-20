package me.dvyy.syncengine.common

import me.dvyy.syncengine.common.mutators.Mutator

class ServerDataStore(
    val store: KeyValueStore = mutableMapOf()
) {
    fun apply(mutator: Mutator) = mutator.mutate(store)
}
