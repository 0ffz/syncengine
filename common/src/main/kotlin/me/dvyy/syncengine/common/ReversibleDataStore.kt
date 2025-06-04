package me.dvyy.syncengine.common

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.flow.MutableStateFlow

class MapBackedKeyValueStore: KeyValueStore {
    val map = mutableMapOf<Long, String?>()
    override suspend operator fun set(key: Long, value: String?) {
        if(value == null) map.remove(key)
        else map.put(key, value)
    }

    override suspend operator fun get(key: Long): String? {
        return map[key]
    }

    fun clear() = map.clear()

    override fun toString(): String = map.toString()
}

data class ReversibleDataStore(
    private val underlying: MapBackedKeyValueStore = MapBackedKeyValueStore(),
    private val diff: MapBackedKeyValueStore = MapBackedKeyValueStore()
): KeyValueStore {
    val changes = Channel<Pair<Long, String?>>()
    fun revert() {
        diff.clear()
    }

    suspend fun setUnderlying(key: Long, value: String?) {
        if(value == null) underlying.remove(key)
        else underlying[key] = value
        changes.send(key to get(key))
    }

    override suspend operator fun set(key: Long, value: String?) {
        diff[key] = value
        changes.send(key to value)
    }

    override suspend fun get(key: Long): String? {
        return diff[key] ?: underlying[key]
    }
}
