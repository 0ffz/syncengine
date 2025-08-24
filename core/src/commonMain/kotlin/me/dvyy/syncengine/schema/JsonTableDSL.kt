package me.dvyy.syncengine.schema

import me.dvyy.sqlite.tables.Table
import me.dvyy.sqlite.tables.View

class ViewBuilder(
    val from: Table,
    val name: String
) {
    val columns = mutableListOf<Column>()

    fun viewStatement(from: String) = """
        SELECT
        id,
        ${columns.joinToString(",\n") { it.toStatement() }}
        FROM $from
        WHERE data != jsonb('null')
    """.trimIndent()

    fun indexStatements() = columns.joinToString(";\n") {
        "CREATE INDEX ${name}_${it.name} ON $name(data ->> '$.${it.name}')"
    }

    fun text(name: String) {
        columns += Column(name, SqliteDataType.TEXT)
    }

    fun integer(name: String) {
        columns += Column(name, SqliteDataType.INTEGER)
    }

    fun real(name: String) {
        columns += Column(name, SqliteDataType.REAL)
    }

    fun build(): View = View(name, viewStatement(from.name), involves = setOf(from))
}

fun jsonTable(name: String): JsonTable {
    return JsonTable(name)
}

fun view(
    name: String,
    table: JsonTable,
    block: ViewBuilder.() -> Unit,
): View {
    return ViewBuilder(table, name).apply(block).build()
}

fun schema(
    shared: Set<JsonTable> = setOf(),
    views: Set<View> = setOf(),
): Schema {
    return Schema(shared.toList(), views.toList())
}
