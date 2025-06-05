package me.dvyy.syncengine.common

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