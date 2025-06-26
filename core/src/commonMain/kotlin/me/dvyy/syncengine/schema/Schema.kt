@file:OptIn(ExperimentalUuidApi::class)

package me.dvyy.syncengine.schema

import me.dvyy.syncengine.db.Database
import me.dvyy.syncengine.db.tables.View
import kotlin.uuid.ExperimentalUuidApi

class Schema(
    val syncedTables: List<JsonTable>,
    val views: List<View>,
) {
    suspend fun initialize() = Database.write {
        syncedTables.forEach { it.create() }
        views.forEach { it.create() }
    }
}
