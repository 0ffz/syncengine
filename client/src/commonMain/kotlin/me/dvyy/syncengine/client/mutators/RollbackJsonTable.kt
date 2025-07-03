package me.dvyy.syncengine.client.mutators

import me.dvyy.syncengine.db.WriteTransaction
import me.dvyy.syncengine.db.tables.Table
import me.dvyy.syncengine.schema.JsonTable

class RollbackJsonTable(
    from: JsonTable,
) : Table(
    """
    CREATE TABLE IF NOT EXISTS ${from.name} (
        id BLOB PRIMARY KEY,
        data BLOB,
        original_data BLOB
    ) STRICT;
    """.trimIndent(),
) {
    override val name = from.name

    context(tx: WriteTransaction)
    override fun create() {
        super.create()
        createTriggers()
    }

    context(tx: WriteTransaction)
    fun rollback() {
        tx.exec(
            """
            DELETE FROM $name WHERE original_data = jsonb('null');
            UPDATE $name SET
                data = original_data,
                original_data = NULL
            WHERE original_data IS NOT NULL;
        """.trimIndent()
        )
    }

    override fun toString(): String = name

    context(tx: WriteTransaction)
    private fun createTriggers() {
        //TODO manage updating as needed
        tx.exec(
            """
            CREATE TRIGGER IF NOT EXISTS ${name}_redirect_delete
                BEFORE DELETE
                ON $name
                FOR EACH ROW
                WHEN old.original_data IS NOT jsonb('null')
            BEGIN
                UPDATE $name SET data = jsonb('null') WHERE id = old.id;
                SELECT raise(ignore);
            END;
            """.trimIndent()
        )

        tx.exec(
            """
            CREATE TRIGGER IF NOT EXISTS ${name}_redirect_update
                BEFORE UPDATE
                ON $name
                FOR EACH ROW
                WHEN old.original_data IS NULL
            BEGIN
                UPDATE $name SET original_data = old.data WHERE id = old.id;
            END;
            """.trimIndent()
        )

        tx.exec(
            """
            CREATE TRIGGER IF NOT EXISTS ${name}_redirect_insert
                AFTER INSERT
                ON $name
                FOR EACH ROW
            BEGIN
                UPDATE $name SET original_data = jsonb('null') WHERE id = new.id;
            END;
            """.trimIndent()
        )
    }
}
