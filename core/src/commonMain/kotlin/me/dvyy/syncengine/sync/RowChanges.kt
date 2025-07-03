package me.dvyy.syncengine.sync

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlin.uuid.Uuid

@Serializable
data class RowChanges(
    val table: String,
    val row: Uuid,
    val data: JsonElement,
)