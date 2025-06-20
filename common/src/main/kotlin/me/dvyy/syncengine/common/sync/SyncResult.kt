package me.dvyy.syncengine.common.sync

import kotlinx.serialization.Serializable

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
