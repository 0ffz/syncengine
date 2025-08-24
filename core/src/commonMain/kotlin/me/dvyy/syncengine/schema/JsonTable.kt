package me.dvyy.syncengine.schema

import me.dvyy.sqlite.tables.Table

enum class SqliteDataType {
    NULL, INTEGER, REAL, TEXT, BLOB
}

class JsonTable(
    override val name: String,
) : Table(
    """
    CREATE TABLE IF NOT EXISTS $name (
        id BLOB PRIMARY KEY,
        data BLOB
    ) STRICT;
    """.trimIndent(),
) {
    val columns: Set<String> = setOf("id", "data")
}
