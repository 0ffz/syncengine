package me.dvyy.syncengine.schema

import me.dvyy.syncengine.db.tables.Table

enum class SqliteDataType {
    NULL, INTEGER, REAL, TEXT, BLOB
}

class JsonTable(
    name: String,
    val extractedColumns: List<Column>,
) : Table(
    """
    CREATE TABLE IF NOT EXISTS $name (
        id BLOB PRIMARY KEY,
        data BLOB
    )
    """.trimIndent(),
) {
    fun viewStatement(from: String) = """
        SELECT
        id,
        ${extractedColumns.joinToString(",\n") { it.toStatement() }}
        FROM $from
    """.trimIndent()

    fun indexStatements() = extractedColumns.joinToString(";\n") {
        "CREATE INDEX ${name}_${it.name} ON $name(data ->> '$.${it.name}')"
    }

    val columns: Set<String> = setOf("id", "data")
}

class TableBuilder(val name: String) {
    val columns = mutableListOf<Column>()

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

    fun buildNamed(name: String = this.name) = JsonTable(
        name, columns.toList()
    )

    fun build() = RollbackTable(this)
}

fun table(name: String, block: TableBuilder.() -> Unit): RollbackTable {
    return TableBuilder(name).apply(block).build()
}
