package me.dvyy.syncengine.schema

import me.dvyy.syncengine.db.WriteTransaction

context(tx: WriteTransaction)
fun createSchema(
    vararg tables: RollbackTable
) {
    tables.forEach { it.create() }
}