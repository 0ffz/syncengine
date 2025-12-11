package me.dvyy.syncengine.jsonactions.reducers

import me.dvyy.syncengine.jsonactions.JsonDataQueries
import me.dvyy.syncengine.jsonactions.actions.DeleteRowAction
import me.dvyy.syncengine.jsonactions.actions.JsonCreateAction
import me.dvyy.syncengine.jsonactions.actions.JsonPatchAction
import me.dvyy.syncengine.reducers.MutableReducers

fun MutableReducers.jsonReducers(
    startId: Int,
    tables: List<JsonDataQueries<*>>,
) {
    val byTable: Map<String, JsonDataQueries<*>> = tables.associateBy { it.table.name }
    fun getTable(name: String) = byTable[name] ?: error("Table $name not found")
    reduce<DeleteRowAction>(startId) {
        getTable(it.table).delete(it.id)
    }
    reduce<JsonCreateAction>(startId + 1) {
        getTable(it.table).create(it.id, it.data)
    }
    reduce<JsonPatchAction>(startId + 2) {
        getTable(it.table).patch(it.id, it.patch.toString())
    }
}