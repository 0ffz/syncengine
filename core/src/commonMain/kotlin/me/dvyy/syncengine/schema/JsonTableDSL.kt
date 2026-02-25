package me.dvyy.syncengine.schema

import me.dvyy.sqlite.WriteTransaction
import me.dvyy.syncengine.reducers.SyncProtocol
import org.intellij.lang.annotations.Language


class ViewBuilder(
    val from: JsonTable,
    val name: String,
    val where: String? = null,
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

    fun build() = JsonView(name, from, columns, where)
}

class JsonView(
    val name: String,
    val from: JsonTable,
    val columns: List<Column>,
    val where: String? = null,
) {
    fun viewStatement(from: String) = """
        SELECT
        id,
        ${columns.joinToString(",\n") { it.toStatement() }}
        FROM $from
        WHERE data != jsonb('null') ${if (where != null) "AND $where" else ""}
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
    @Language("SQLite")
    where: String? = null,
    block: ViewBuilder.() -> Unit,
): JsonView {
    return ViewBuilder(table, name, where).apply(block).build()
}

fun schema(
    shared: Set<JsonTable> = setOf(),
    views: Set<JsonView> = setOf(),
    protocol: SyncProtocol,
): Schema {
    return Schema(shared.toList(), views.toList(), protocol)
}
