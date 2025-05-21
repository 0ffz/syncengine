package me.dvyy.syncengine.common

interface KeyValueStore {
    suspend operator fun set(key: Long, value: String?)
    suspend operator fun get(key: Long): String?
    suspend fun remove(key: Long) = set(key, null)
}
