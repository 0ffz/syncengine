package me.dvyy.syncengine.client.mutators

import me.dvyy.syncengine.db.Database
import me.dvyy.syncengine.schema.Schema

class ClientSchema(schema: Schema) {
    val syncedTables = schema.syncedTables.map { RollbackJsonTable(it) }
    val views = schema.views

    suspend fun initialize() = Database.write {
        syncedTables.forEach { it.create() }
        views.forEach { it.create() }
    }
}


fun Schema.asClientSchema() = ClientSchema(this)
