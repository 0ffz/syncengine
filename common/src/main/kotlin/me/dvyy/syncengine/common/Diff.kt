package me.dvyy.syncengine.common

import kotlinx.serialization.Serializable
import me.dvyy.syncengine.common.ui.Task
import kotlin.uuid.Uuid

@Serializable
data class RowDiff(
    val row: Uuid,
    val value: Task?,
)


@Serializable
sealed interface SyncResult {
    @Serializable
    data class Updates(
        val updates: List<RowDiff>,
        val lastChange: Long,
    ) : SyncResult

    @Serializable
    data class MutatorsAck(val amount: Int) : SyncResult
}
