package me.dvyy.syncengine.common.mutators

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.dvyy.syncengine.common.KeyValueStore
import me.dvyy.syncengine.common.ui.Task

@Serializable
@SerialName("inc")
data class Increment(
    val row: Long,
    val value: Int,
) : Mutator {
    override fun mutate() {
//        store[row] = ((store[row]?.toIntOrNull() ?: 0) + value).toString()
    }

    override fun reduce(previous: Mutator): Mutator? {
//        val p = previous as Increment
//        if (p.row == row) return Increment(row, value + p.value)
        return null
    }
}
