package me.dvyy.syncengine.sync

import kotlinx.serialization.Serializable
import me.dvyy.syncengine.schema.AbstractMutator

@Serializable
data class SyncRequest(
    val lastFrameSeen: Long,
    val mutators: List<AbstractMutator<*>>,
)

