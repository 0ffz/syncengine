package me.dvyy.syncengine.db.tables

import me.dvyy.syncengine.db.WriteTransaction
import org.intellij.lang.annotations.Language

abstract class Table(
    @param:Language("SQLite")
    val createStatement: String,
) : TableReading {
    override val involves: Set<TableReading> = setOf(this)

    override val name: String = nameRegex.find(createStatement)!!.groupValues[1]
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
    override val name: String,
    @param:Language("SQLite")
    val selectStatement: String,
    override val involves: Set<TableReading>,
) : TableReading {

    context(tx: WriteTransaction)
    override fun create() {
        tx.exec("CREATE VIEW IF NOT EXISTS $name AS $selectStatement")
    }
}
