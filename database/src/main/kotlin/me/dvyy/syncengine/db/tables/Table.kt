package me.dvyy.syncengine.db.tables

import org.intellij.lang.annotations.Language

interface TableReading {
    val involves: Set<TableReading>

    companion object {
        fun reduce(tables: Set<TableReading>): Set<Table> = tables.flatMap {
            when (it) {
                is Table -> setOf(it)
                else -> reduce(it.involves)
            }
        }.toSet()
    }
}

abstract class Table(
    @param:Language("SQLite")
    val createStatement: String,
): TableReading {
    override val involves: Set<TableReading> = setOf(this)

    val name: String = nameRegex.find(createStatement)!!.groupValues[1]
    override fun toString(): String = name

    companion object {
        val nameRegex = "CREATE TABLE (?:IF NOT EXISTS\\s+)?(\\w+)".toRegex()
    }
}

abstract class View(
    @param:Language("SQLite")
    val selectStatement: String,
    override val involves: Set<TableReading>,
): TableReading {
    val name = this::class.simpleName!!
    override fun toString(): String = name
}
