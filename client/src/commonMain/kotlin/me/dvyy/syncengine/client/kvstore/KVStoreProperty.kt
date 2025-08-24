package me.dvyy.syncengine.client.kvstore

import me.dvyy.sqlite.Transaction
import me.dvyy.sqlite.WriteTransaction
import me.dvyy.sqlite.tables.TableReading

data class KVStoreProperty(
    val key: String,
    val table: TableReading,
) {
    context(tx: Transaction)
    fun getString(): String? =
        tx.getOrNull("SELECT data FROM $table WHERE id = ?", key) { getText(0) }

    context(tx: WriteTransaction)
    fun setString(value: String) {
        tx.exec("INSERT INTO $table VALUES (?1, ?2) ON CONFLICT DO UPDATE SET data = ?2", key, value)
    }
}
