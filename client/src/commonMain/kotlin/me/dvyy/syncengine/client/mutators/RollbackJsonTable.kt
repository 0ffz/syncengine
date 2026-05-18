package me.dvyy.syncengine.client.mutators

import co.touchlab.kermit.Logger
import me.dvyy.sqlite.WriteTransaction
import me.dvyy.syncengine.schema.JsonTable

class RollbackJsonTable(
    from: JsonTable,
) : JsonTable(from.name, from.indexes) {
    context(tx: WriteTransaction)
    override fun create() {
        tx.exec(
            """
            CREATE TABLE IF NOT EXISTS $name (
                id BLOB PRIMARY KEY,
                data BLOB,
                owner INTEGER NOT NULL,
                original_data BLOB
            ) STRICT;
            """.trimIndent(),
        )
        super.createIndexes()
        createTriggers()
    }

    context(tx: WriteTransaction)
    fun rollback() {
        Logger.v { "[Rollback $name] Deleting where original data is null" }
        tx.exec(
            """
            DELETE FROM $name WHERE original_data = jsonb('null');
            """.trimIndent()
        )

        // Avoids UNIQUE constraint violations
        Logger.v { "[Rollback $name] Clearing data where original_data isn't null" }
        tx.exec(
            """
            UPDATE $name SET
                data = NULL
            WHERE original_data IS NOT NULL;
        """.trimIndent()
        )

        Logger.v { "[Rollback $name] Copying back original_data -> data" }
        tx.exec(
            """
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
                WHEN old.original_data != jsonb('null')
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
