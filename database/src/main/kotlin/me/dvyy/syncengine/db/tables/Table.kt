package me.dvyy.syncengine.db.tables

import org.intellij.lang.annotations.Language

abstract class Table(
    @param:Language("SQLite")
    val createStatement: String,
)

abstract class View(
    @param:Language("SQLite")
    val selectStatement: String,
)
