package me.dvyy.syncengine.sync

import kotlinx.serialization.Serializable

@Serializable
data class SyncResult(
    val serverFrame: Long,
    val changes: List<RowChanges>,
)