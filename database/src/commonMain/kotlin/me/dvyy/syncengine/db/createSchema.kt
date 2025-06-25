package me.dvyy.syncengine.db

import me.dvyy.syncengine.db.tables.Table
import me.dvyy.syncengine.db.tables.View

suspend fun createSchema(tables: List<Table>, views: List<View>) = Database.write {
//    tables.forEach { exec(it.createStatement) }
//    views.forEach { exec() }
}