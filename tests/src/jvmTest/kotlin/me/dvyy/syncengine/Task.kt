package me.dvyy.syncengine

import kotlinx.serialization.Serializable

@Serializable
data class Task(val text: String, val done: Boolean = false)