package me.dvyy.syncengine.client.mutators

import me.dvyy.syncengine.db.tables.Table

object MutatorsTable : Table(
    """
    CREATE TABLE IF NOT EXISTS mutators(
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        data BLOB NOT NULL
    ) STRICT
    """.trimIndent()
)
