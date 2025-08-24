@file:OptIn(ExperimentalTime::class)

package me.dvyy.syncengine

import me.dvyy.sqlite.Database
import me.dvyy.syncengine.schema.ServerSchema
import kotlin.time.ExperimentalTime

class SyncServerDataStore(
    val db: Database,
    val schema: ServerSchema,
) {
//    val timestampCol = with(TaskTable) { long("timestamp").clientDefault { Clock.System.now().toEpochMilliseconds() } }

//    fun apply(mutator: Mutator) = mutator.mutate()
//
//    fun apply(mutators: List<Mutator>) = mutators.forEach {
//        apply(it)
//    }
//
//    @OptIn(ExperimentalUuidApi::class)
//    fun getUpdatedSince(timestamp: Long): SyncResult.Updates = transaction(db) {
//        val updates = TaskTable.selectAll()
//            .where { timestampCol greaterEq timestamp }
//            .map {
//                RowDiff(
//                    it[TaskTable.id].value.toKotlinUuid(),
//                    Task(it[TaskTable.name], it[TaskTable.done])
//                )
//            }
//            .toList()
//        val max = timestampCol.max()
//        val lastUpdate: Long = TaskTable.select(max)
//            .singleOrNull()
//            ?.get(max)
//            ?: timestamp
//        SyncResult.Updates(
//            updates,
//            lastUpdate
//        )
//    }
}
