package me.dvyy.syncengine.sync

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class SyncRequest(
    val deviceId: Uuid,
    val lastFrameSeen: Long,
    val encodedActions: List<ByteArray>,
    val firstActionId: Long,
)

