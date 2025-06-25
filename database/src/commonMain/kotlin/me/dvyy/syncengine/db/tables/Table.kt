package me.dvyy.syncengine.db.tables

import me.dvyy.syncengine.db.WriteTransaction
import org.intellij.lang.annotations.Language

interface TableReading {
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

abstract class Table(
    @param:Language("SQLite")
    val createStatement: String,
) : TableReading {
    override val involves: Set<TableReading> = setOf(this)

    val name: String = nameRegex.find(createStatement)!!.groupValues[1]
    override fun toString(): String = name

    context(tx: WriteTransaction)
    override fun create() {
        tx.exec(createStatement)
    }

    companion object {
        val nameRegex = "CREATE TABLE (?:IF NOT EXISTS\\s+)?(\\w+)".toRegex()
    }
}

open class View(
    val name: String,
    @param:Language("SQLite")
    val selectStatement: String,
    override val involves: Set<TableReading>,
) : TableReading {
    override fun toString(): String = name

    context(tx: WriteTransaction)
    override fun create() {
        tx.exec("CREATE VIEW IF NOT EXISTS $name AS $selectStatement")
    }
}
