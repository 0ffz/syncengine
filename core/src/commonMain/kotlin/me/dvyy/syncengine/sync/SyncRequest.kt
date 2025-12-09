package me.dvyy.syncengine.sync

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class SyncRequest(
    val deviceId: Uuid,
    val lastFrameSeen: Long,
    val encodedMutators: List<ByteArray>,
    val firstMutatorId: Long,
)

