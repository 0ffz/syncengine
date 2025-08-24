package me.dvyy.syncengine.client.kvstore

import me.dvyy.sqlite.tables.Table

val KVStore = Table(
    """
    CREATE TABLE IF NOT EXISTS KVStore (
        id TEXT PRIMARY KEY,
        data TEXT
    ) STRICT
    """.trimIndent()
)
