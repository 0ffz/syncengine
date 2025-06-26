package me.dvyy.syncengine.db.tables

import me.dvyy.syncengine.db.WriteTransaction

interface TableReading {
    val name: String
    val involves: Set<TableReading>

    context(tx: WriteTransaction)
    fun create()

    companion object {
        fun reduce(tables: Set<TableReading>): Set<Table> = tables.flatMap {
            when (it) {
                is Table -> setOf(it)
                else -> reduce(it.involves)
            }
        }.toSet()
    }
}
