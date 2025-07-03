package me.dvyy.syncengine.schema

import me.dvyy.syncengine.db.WriteTransaction
import me.dvyy.syncengine.db.tables.View

class ServerSchema(schema: Schema) {
    val syncedTables: List<UserRestrictedJsonTable> = schema.syncedTables.map { UserRestrictedJsonTable(it) }
    val views: List<View> = schema.views

    context(tx: WriteTransaction)
    fun initialize() {
        syncedTables.forEach { it.create() }
        views.forEach { it.create() }
    }
}

fun Schema.asServerSchema() = ServerSchema(this)