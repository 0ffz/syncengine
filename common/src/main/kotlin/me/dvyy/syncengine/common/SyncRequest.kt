package me.dvyy.syncengine.common

import kotlinx.serialization.Serializable
import me.dvyy.syncengine.common.mutators.Mutator

@Serializable
sealed interface SyncRequest {
    @Serializable
    class ChangesSince(val timestamp: Long) : SyncRequest

    @Serializable
    class ApplyMutators(
        val mutators: List<Mutator>,
        val lastSync: Long
    ) : SyncRequest
}
