package me.dvyy.syncengine.common

import kotlinx.coroutines.flow.MutableSharedFlow

data class ReversibleDataStore(
    private val underlying: MapBackedKeyValueStore = MapBackedKeyValueStore(),
    private val diff: MapBackedKeyValueStore = MapBackedKeyValueStore()
): KeyValueStore {
    val changes = MutableSharedFlow<Pair<Long, String?>>()
    fun revert() {
        diff.clear()
    }

    suspend fun setUnderlying(key: Long, value: String?) {
        if(value == null) underlying.remove(key)
        else underlying[key] = value
        changes.emit(key to get(key))
    }

    override suspend operator fun set(key: Long, value: String?) {
        diff[key] = value
        changes.emit(key to value)
    }

    override suspend fun get(key: Long): String? {
        return diff[key] ?: underlying[key]
    }
}
