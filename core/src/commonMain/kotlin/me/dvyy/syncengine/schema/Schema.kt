@file:OptIn(ExperimentalUuidApi::class)

package me.dvyy.syncengine.schema

import kotlin.uuid.ExperimentalUuidApi

class Schema(
    val syncedTables: List<JsonTable>,
    val views: List<JsonView>,
)
