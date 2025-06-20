package me.dvyy.syncengine.common.sync

import kotlinx.serialization.Serializable
import me.dvyy.syncengine.common.tables.Task
import kotlin.uuid.Uuid

@Serializable
data class RowDiff(
    val row: Uuid,
    val value: Task?,
)
