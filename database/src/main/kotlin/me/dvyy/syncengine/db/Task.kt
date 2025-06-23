package me.dvyy.syncengine.db

import kotlinx.serialization.Serializable

@Serializable
data class Task(val name: String, val done: Boolean = false)