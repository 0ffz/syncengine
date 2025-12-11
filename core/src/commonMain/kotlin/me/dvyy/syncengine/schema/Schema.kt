@file:OptIn(ExperimentalUuidApi::class)

package me.dvyy.syncengine.schema

import me.dvyy.syncengine.reducers.SyncProtocol
import kotlin.uuid.ExperimentalUuidApi

class Schema(
    val syncedTables: List<JsonTable>,
    val views: List<JsonView>,
    val protocol: SyncProtocol,
)
