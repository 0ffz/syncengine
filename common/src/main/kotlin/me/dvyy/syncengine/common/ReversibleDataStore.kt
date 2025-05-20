package me.dvyy.syncengine.common

data class ReversibleDataStore(
    private val underlying: KeyValueStore = mutableMapOf(),
    private val diff: KeyValueStore = mutableMapOf()
): KeyValueStore {
    fun revert() {
        diff.clear()
    }

    fun putUnderlying(key: Long, value: String?) {
        if(value == null) underlying.remove(key)
        else underlying[key] = value
    }

    override val keys: MutableSet<Long>
        get() = TODO("Not yet implemented")
    override val values: MutableCollection<String>
        get() = TODO("Not yet implemented")
    override val entries: MutableSet<MutableMap.MutableEntry<Long, String>>
        get() = TODO("Not yet implemented")

    override fun put(key: Long, value: String): String? = diff.put(key, value)

    override fun remove(key: Long): String? {
        return diff.remove(key) //TODO removal stub
    }

    override fun putAll(from: Map<out Long, String>) {
        TODO("Not yet implemented")
    }

    override fun clear() {
        TODO("Not yet implemented")
    }

    override val size: Int
        get() = TODO("Not yet implemented")

    override fun isEmpty(): Boolean {
        TODO("Not yet implemented")
    }

    override fun containsKey(key: Long): Boolean {
        TODO("Not yet implemented")
    }

    override fun containsValue(value: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun get(key: Long): String? {
        return diff[key] ?: underlying[key]
    }
}
