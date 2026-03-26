package me.dvyy.syncengine.jsonactions.reducers

import me.dvyy.syncengine.jsonactions.JsonDataQueries
import me.dvyy.syncengine.jsonactions.actions.*
import me.dvyy.syncengine.reducers.MutableReducers

fun MutableReducers.jsonReducers(
    tables: List<JsonDataQueries<*>>,
) {
    val byTable: Map<String, JsonDataQueries<*>> = tables.associateBy { it.table.name }
    fun getTable(name: String) = byTable[name] ?: error("Table $name not found")

    reduce<DeleteEntityAction> { action ->
        byTable.values.forEach { it.delete(action.id) }
    }
    reduce<DeleteRowAction> {
        getTable(it.table).delete(it.id)
    }
    reduce<JsonCreateAction> {
        getTable(it.table).create(it.id, it.data)
    }
    reduce<JsonPatchAction> {
        getTable(it.table).patch(it.id, it.patch.toString())
    }
    reduce<JsonSetAction> {
        getTable(it.table).jsonSet(it.id, it.path, it.patch.toString())
    }
}