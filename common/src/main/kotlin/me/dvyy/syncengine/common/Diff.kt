package me.dvyy.syncengine.common

import kotlinx.serialization.Serializable

@Serializable
data class RowDiff(
    val row: Long,
    val value: String?
)
