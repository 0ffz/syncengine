package me.dvyy.syncengine.schema

import me.dvyy.sqlite.WriteTransaction


class ViewBuilder(
    val from: JsonTable,
    val name: String,
) {
    val columns = mutableListOf<Column>()

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

    fun blob(name: String) {
        columns += Column(name, SqliteDataType.BLOB)
    }

    fun build() = JsonView(name, from, columns)
}

class JsonView(val name: String, val from: JsonTable, val columns: List<Column>) {
    fun viewStatement(from: String) = """
        SELECT
        id,
        ${columns.joinToString(",\n") { it.toStatement() }}
        FROM $from
        WHERE data != jsonb('null')
    """.trimIndent()

    context(tx: WriteTransaction)
    fun create() {
        tx.exec("CREATE VIEW IF NOT EXISTS $name AS ${viewStatement(from.name)}")
    }
}

fun jsonTable(name: String): JsonTable {
    return JsonTable(name)
}

fun view(
    name: String,
    table: JsonTable,
    block: ViewBuilder.() -> Unit,
): JsonView {
    return ViewBuilder(table, name).apply(block).build()
}

fun schema(
    shared: Set<JsonTable> = setOf(),
    views: Set<JsonView> = setOf(),
): Schema {
    return Schema(shared.toList(), views.toList())
}
