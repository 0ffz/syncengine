@file:OptIn(ExperimentalUuidApi::class)

package me.dvyy.syncengine.common.mutators

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.dvyy.syncengine.common.KeyValueStore
import kotlin.uuid.ExperimentalUuidApi

@Serializable
@SerialName("mutateText")
data class MutateText(
    val row: Long,
    val insertAfter: Int,
    val text: String,
) : Mutator {
    override suspend fun mutate(store: KeyValueStore) {
        val existing = store[row]
//        val new = if(existing == null) text else existing.take(insertAfter) + text + existing.drop(insertAfter)
        store[row] = text//new
    }
}