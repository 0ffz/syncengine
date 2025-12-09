package me.dvyy.syncengine.schema

import me.dvyy.sqlite.WriteTransaction

enum class SqliteDataType {
    NULL, INTEGER, REAL, TEXT, BLOB
}

open class JsonTable(val name: String) {
    context(tx: WriteTransaction)
    open fun create() {
        tx.exec(
            """
            CREATE TABLE IF NOT EXISTS $name (
                id BLOB PRIMARY KEY,
                data BLOB
            ) STRICT;
            """.trimIndent()
        )
    }

    override fun toString(): String {
        return name
    }
}
