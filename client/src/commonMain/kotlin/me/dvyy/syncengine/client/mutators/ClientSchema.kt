package me.dvyy.syncengine.client.mutators

import me.dvyy.sqlite.Database
import me.dvyy.sqlite.WriteTransaction
import me.dvyy.syncengine.client.kvstore.KVStore
import me.dvyy.syncengine.schema.Schema

class ClientSchema(
    schema: Schema,
    val db: Database,
) {
    val syncedTables = schema.syncedTables.map { RollbackJsonTable(it) }
    val views = schema.views

    suspend fun initialize() = db.write {
        MutatorsTable.create()
        KVStore.create()
        syncedTables.forEach { it.create() }
        views.forEach { it.create() }
    }

    context(tx: WriteTransaction)
    fun rollbackAll() {
        syncedTables.forEach { it.rollback() }
    }
}

fun Schema.asClientSchema(db: Database) = ClientSchema(this, db)
