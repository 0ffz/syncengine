package me.dvyy.syncengine.client.mutators

import me.dvyy.syncengine.db.WriteTransaction
import me.dvyy.syncengine.db.tables.TableReading
import me.dvyy.syncengine.db.tables.View
import me.dvyy.syncengine.schema.JsonTable

class RollbackJsonTable(
    from: JsonTable,
) : TableReading {
    override val name = from.name
    val underlying = JsonTable("${name}_underlying")
    val overlay = JsonTable("${name}_overlay")
    override val involves: Set<TableReading> = setOf(underlying, overlay)

    context(tx: WriteTransaction)
    override fun create() {
        underlying.create()
        overlay.create()
        merged.create()
        createTriggers()
    }

    private val merged: View = View(
        name,
        """
        SELECT ${underlying.columns.joinToString(",") { "coalesce(o.$it, u.$it) as $it" }}
        FROM $underlying u
        FULL OUTER JOIN $overlay o
        ON u.id = o.id
        WHERE o.data is not jsonb('null')
        """.trimIndent(),
        involves = setOf(underlying, overlay)
    )

    context(tx: WriteTransaction)
    fun rollback() {
        tx.exec("DELETE FROM $overlay")
    }

    override fun toString(): String = name

    context(tx: WriteTransaction)
    private fun createTriggers() {
        tx.exec(
            """
            CREATE TRIGGER IF NOT EXISTS ${merged}_redirect_insert
            INSTEAD OF INSERT ON $merged
            FOR EACH ROW
            BEGIN
                INSERT INTO $overlay (${overlay.columns.joinToString(",")})
                VALUES (${underlying.columns.joinToString(",") { "NEW.${it}" }});
            END;
            """.trimIndent()
        )

        tx.exec(
            """
            CREATE TRIGGER  IF NOT EXISTS ${merged}_redirect_delete
            INSTEAD OF DELETE ON $merged
            FOR EACH ROW
            BEGIN
                INSERT INTO $overlay (id, data)
                VALUES (OLD.id, jsonb('null'))
                ON CONFLICT(id) DO UPDATE SET data = jsonb('null');
            END;
            """.trimIndent()
        )

        tx.exec(
            """
            CREATE TRIGGER  IF NOT EXISTS ${merged}_redirect_update
            INSTEAD OF UPDATE ON $merged
            FOR EACH ROW
            BEGIN
                INSERT INTO $overlay (${overlay.columns.joinToString(",")})
                VALUES (${underlying.columns.joinToString(",") { "NEW.${it}" }})
                ON CONFLICT DO UPDATE SET ${underlying.columns.joinToString(",") { "${it} = NEW.${it}" }};
            END;
            """.trimIndent()
        )
    }
}
