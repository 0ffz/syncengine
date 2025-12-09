package me.dvyy.syncengine.client.kvstore

import me.dvyy.sqlite.Transaction
import me.dvyy.sqlite.WriteTransaction

data class KVStoreProperty(
    val key: String,
) {
    context(tx: Transaction)
    fun getString(): String? =
        tx.getOrNull("SELECT data FROM KVStore WHERE id = ?", key) { getText(0) }

    context(tx: WriteTransaction)
    fun setString(value: String) {
        tx.exec("INSERT INTO KVStore VALUES (?1, ?2) ON CONFLICT DO UPDATE SET data = ?2", key, value)
    }
}
