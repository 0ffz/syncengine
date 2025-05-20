package me.dvyy.syncengine.common.mutators

import kotlinx.serialization.Serializable
import me.dvyy.syncengine.common.KeyValueStore

@Serializable
sealed interface Mutator {
    val id: Int

    fun mutate(store: KeyValueStore)

    fun reduce(previous: Mutator): Mutator? = null
}
