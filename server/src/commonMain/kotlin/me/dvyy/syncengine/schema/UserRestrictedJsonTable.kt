package me.dvyy.syncengine.schema

import me.dvyy.sqlite.WriteTransaction
import me.dvyy.sqlite.tables.Table

class UserRestrictedJsonTable(from: JsonTable) : Table(
    """
    CREATE TABLE IF NOT EXISTS ${from.name} (
        id BLOB NOT NULL PRIMARY KEY,
        data BLOB,
        owner INTEGER NOT NULL,
        frame INTEGER NOT NULL DEFAULT (strftime('%s','now') || substr(strftime('%f','now'),4))
    ) STRICT;
    """.trimIndent()
) {
    override val name: String = from.name

    override val involves = setOf(from)

    context(tx: WriteTransaction)
    override fun create() {
        super.create()
        createIndexes()
    }

    context(tx: WriteTransaction)
    fun createIndexes() {
        tx.exec("CREATE INDEX IF NOT EXISTS ${name}_owner ON $name(owner);")
        tx.exec(
            """
            CREATE TRIGGER IF NOT EXISTS ${name}_frame_update
                BEFORE UPDATE ON $name
                FOR EACH ROW
            BEGIN
                UPDATE $name SET frame = (strftime('%s','now') || substr(strftime('%f','now'),4))
                WHERE id = old.id;
            END;
        """.trimIndent()
        )
    }
}
