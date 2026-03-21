package me.dvyy.syncengine.schema

import me.dvyy.sqlite.WriteTransaction

enum class SqliteDataType {
    NULL, INTEGER, REAL, TEXT, BLOB
}

open class JsonTable(
    val name: String,
    val indexes: List<TableIndex>,
) {
    init {
        // Ensure indexes have unique names
        val names = indexes.map { it.nameSuffix }
        require(names == names.distinct())
    }

    private fun getName(index: TableIndex) = "auto_${name}_${index.nameSuffix}"

    context(tx: WriteTransaction)
    open fun create() {
        tx.exec(
            """
            CREATE TABLE IF NOT EXISTS $name (
                id BLOB PRIMARY KEY,
                data BLOB
            ) STRICT;
            """.trimIndent()
        )
        createIndexes()
    }

    context(tx: WriteTransaction)
    fun createIndexes() {
        val existingIndexes = tx.select("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name=?", name)
            .map { getText(0) }
            .filter { it.startsWith("auto_${name}_") }
            .toSet()
        val existingStatements = existingIndexes.associateWith { indexName ->
            tx.select("SELECT sql FROM sqlite_master WHERE type='index' AND name=?", indexName)
                .map { getText(0) }
                .firstOrNull() ?: ""
        }
        val newIndexes = indexes
            .associate { getName(it) to it.createStatement(tableName = name) }
            .toMutableMap()

        existingStatements.forEach { (name, statement) ->
            if (newIndexes[name] != statement) tx.exec("DROP INDEX $name")
            else newIndexes.remove(name)
        }
        newIndexes.forEach { (_, statement) -> tx.exec(statement) }
    }

    override fun toString(): String {
        return name
    }
}
