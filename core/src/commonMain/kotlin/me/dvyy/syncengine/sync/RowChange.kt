package me.dvyy.syncengine.sync

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class TableChanges(
    val table: String,
    val changes: List<RowChange>,
    val deletions: List<Uuid>,
)

@Serializable
data class RowChange(
    val row: Uuid,
    val data: ByteArray,
) {
    override fun toString(): String {
        return "RowChange(row=$row,data=${data.size} bytes)"
    }
}
