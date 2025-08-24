package me.dvyy.syncengine.sync

import kotlinx.serialization.Serializable

@Serializable
data class SyncRequest(
    val lastFrameSeen: Long,
    val encodedMutators: List<ByteArray>,
    val firstMutatorId: Long?,
)

