package me.dvyy.syncengine.common

import me.dvyy.syncengine.common.mutators.MutatorsTable.default
import me.dvyy.syncengine.common.ui.TaskTable
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.*
import kotlin.uuid.toJavaUuid

class DiffableTables(
    val merged: IdTable<*>,
    constructor: (String) -> IdTable<*>,
) {
    val underlying = constructor("${merged.tableName}_underlying")
    val overlay = constructor("${merged.tableName}_overlay")

    fun rollback() {
        overlay.deleteAll()
    }

    fun setUnderlying(diff: List<RowDiff>) {
        diff.forEach { (row, value) ->
            val id = (underlying.id as Column<EntityID<UUID>>)
            val matchesId = id eq row.toJavaUuid()
            if (value == null) underlying.deleteWhere { matchesId }
            else {
                val count = underlying.update(where = { matchesId }) {
                    it[TaskTable.name] = value.name
                    it[TaskTable.done] = value.done
                }
                if (count == 0) underlying.insert {
                    it[id] = row.toJavaUuid()
                    it[TaskTable.name] = value.name
                    it[TaskTable.done] = value.done
                }
            }
        }
    }

    fun JdbcTransaction.initialize() {
        val viewName = merged.tableName

        with(overlay) {
            overlay.columns.toList().forEach {
                if (it != overlay.id) (it as Column<Any>).nullable()
            }
        }
        val overlayRemovedField = overlay.bool("removed").default(false)
        transaction {
            SchemaUtils.create(underlying, overlay)
        }
        val mergedView = underlying.leftJoin(overlay, onColumn = { underlying.id }, otherColumn = { overlay.id })
            .select(
                underlying.columns.zip(overlay.columns).map { (it, other) ->
                    if (it == underlying.id) it else Coalesce(other, it).alias("\"${it.name}\"")
                })
            .where { overlayRemovedField.isDistinctFrom(booleanLiteral(true)) }
            .unionAll(
                overlay.select(overlay.columns.minus(overlayRemovedField))
                    .where {
                        notExists(
                            underlying.select(byteLiteral(1)).where(underlying.id eq overlay.id)
                        ).and { overlayRemovedField.isDistinctFrom(booleanLiteral(true)) }
                    }
            )
            .prepareSQL(this).also { println(it) }
        exec("CREATE VIEW IF NOT EXISTS $viewName AS $mergedView")
        exec(
            """
        CREATE TRIGGER  IF NOT EXISTS ${viewName}_redirect_insert
        INSTEAD OF INSERT ON $viewName
        FOR EACH ROW
        BEGIN
            INSERT INTO ${overlay.tableName} (${overlay.columns.joinToString(",") { it.name }})
            VALUES (${underlying.columns.joinToString(",") { "NEW.${it.name}" }}, FALSE);
        END;
    """.trimIndent()
        )

        exec(
            """
        CREATE TRIGGER  IF NOT EXISTS ${viewName}_redirect_delete
        INSTEAD OF DELETE ON $viewName
        FOR EACH ROW
        BEGIN
            INSERT INTO ${overlay.tableName} (id, removed)
            VALUES (OLD.id, TRUE)
            ON CONFLICT(id) DO UPDATE SET removed = TRUE;
        END;
    """.trimIndent()
        )

        exec(
            """
        CREATE TRIGGER  IF NOT EXISTS ${viewName}_redirect_update
        INSTEAD OF UPDATE ON $viewName
        FOR EACH ROW
        BEGIN
            INSERT INTO ${overlay.tableName} (${overlay.columns.joinToString(",") { it.name }})
            VALUES (${underlying.columns.joinToString(",") { "NEW.${it.name}" }}, FALSE)
            ON CONFLICT DO UPDATE SET ${underlying.columns.joinToString(",") { "${it.name} = NEW.${it.name}" }};
        END;
    """.trimIndent()
        )
    }
}