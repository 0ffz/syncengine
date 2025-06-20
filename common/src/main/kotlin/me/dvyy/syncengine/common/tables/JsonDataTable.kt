package me.dvyy.syncengine.common.tables

import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class JsonDataTable: UUIDTable() {
    val data = text("data") //TODO use binary for sqlite
}

data class JsonColumn(
    val name: String,
)
abstract class JsonView(
    val dataTable: JsonDataTable
) {
    fun column(name: String, path: String = "$.$name", type: String): JsonColumn {
        "cast(data ->> '$path' as $type) as $name"
        TODO()
    }
}


object Tasks: JsonView(JsonDataTable()) {
    val name = column("name", type = "TEXT")
    val done = column("done", type = "BOOLEAN")

    fun test() = transaction {
//        name.
    }
}
