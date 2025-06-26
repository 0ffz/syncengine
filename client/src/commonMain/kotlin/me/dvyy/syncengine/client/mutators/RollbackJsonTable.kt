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

//    context(tx: WriteTransaction)
//    fun createTriggers() {
//        tx.exec("CREATE VIEW IF NOT EXISTS $viewName AS $mergedView")
//        tx.exec(
//            """
//        CREATE TRIGGER  IF NOT EXISTS ${viewName}_redirect_insert
//        INSTEAD OF INSERT ON $viewName
//        FOR EACH ROW
//        BEGIN
//            INSERT INTO ${overlay.tableName} (${overlay.columns.joinToString(",") { it.name }})
//            VALUES (${underlying.columns.joinToString(",") { "NEW.${it.name}" }}, FALSE);
//        END;
//    """.trimIndent()
//        )
//
//        exec(
//            """
//        CREATE TRIGGER  IF NOT EXISTS ${viewName}_redirect_delete
//        INSTEAD OF DELETE ON $viewName
//        FOR EACH ROW
//        BEGIN
//            INSERT INTO ${overlay.tableName} (id, removed)
//            VALUES (OLD.id, TRUE)
//            ON CONFLICT(id) DO UPDATE SET removed = TRUE;
//        END;
//    """.trimIndent()
//        )
//
//        exec(
//            """
//        CREATE TRIGGER  IF NOT EXISTS ${viewName}_redirect_update
//        INSTEAD OF UPDATE ON $viewName
//        FOR EACH ROW
//        BEGIN
//            INSERT INTO ${overlay.tableName} (${overlay.columns.joinToString(",") { it.name }})
//            VALUES (${underlying.columns.joinToString(",") { "NEW.${it.name}" }}, FALSE)
//            ON CONFLICT DO UPDATE SET ${underlying.columns.joinToString(",") { "${it.name} = NEW.${it.name}" }};
//        END;
//    """.trimIndent()
//        )
//    }
}
