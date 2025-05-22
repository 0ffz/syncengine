package me.dvyy.syncengine.common

import kotlinx.serialization.Serializable

@Serializable
data class RowDiff(
    val row: Long,
    val value: String?
)


@Serializable
sealed interface SyncResult {
    @Serializable
    data class Updates(
        val updates: List<RowDiff>,
        val lastTimestamp: Long,
    ): SyncResult

    @Serializable
    data class MutatorsAck(val amount: Int): SyncResult
}
