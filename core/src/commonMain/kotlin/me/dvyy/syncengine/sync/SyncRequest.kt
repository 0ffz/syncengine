package me.dvyy.syncengine.sync

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class SyncRequest(
    val deviceId: Uuid,
    val lastFrameSeen: Long,
    val encodedActions: List<ByteArray>,
    val firstActionId: Long,
) {
    val lastActionId = firstActionId + encodedActions.lastIndex
    override fun toString(): String {
        return "SyncRequest(deviceId=$deviceId, lastFrameSeen=$lastFrameSeen, encodedActions=${encodedActions.size}, firstActionId=$firstActionId)"
    }
}

