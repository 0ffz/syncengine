package me.dvyy.syncengine.common.mutators

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.dvyy.syncengine.common.ui.Task
import me.dvyy.syncengine.common.ui.TaskEntity
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

@Serializable
sealed interface Mutator {
    fun mutate()

    fun reduce(previous: Mutator): Mutator? = null
}

@Serializable
@SerialName("set")
data class UpdateTask(
    val row: Uuid,
    val data: Task,
) : Mutator {
    override fun mutate() {
        TaskEntity.findByIdAndUpdate(row.toJavaUuid()) { it.done = data.done; it.name = data.name }
    }
}