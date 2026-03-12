package me.dvyy.syncengine.server.schema

import kotlinx.serialization.Serializable
import me.dvyy.sqlite.Identity

@Serializable
data class WorkspaceModel(
    val name: String,
    val owner: Identity,
)