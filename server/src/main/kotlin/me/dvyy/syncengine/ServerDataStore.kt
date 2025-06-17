@file:OptIn(ExperimentalTime::class)

package me.dvyy.syncengine

import kotlinx.datetime.Clock
import me.dvyy.syncengine.common.RowDiff
import me.dvyy.syncengine.common.SyncResult
import me.dvyy.syncengine.common.mutators.Mutator
import me.dvyy.syncengine.common.ui.Task
import me.dvyy.syncengine.common.ui.TaskEntity
import me.dvyy.syncengine.common.ui.TaskTable
import org.jetbrains.exposed.v1.core.max
import org.jetbrains.exposed.v1.dao.EntityChangeType
import org.jetbrains.exposed.v1.dao.EntityHook
import org.jetbrains.exposed.v1.dao.toEntity
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

class ServerDataStore {
    //    val db =R2dbcDatabase.connect("r2dbc:h2:mem:///regular;DB_CLOSE_DELAY=-1;")
    val db = Database.connect("jdbc:sqlite:server.db")
    val timestampCol = with(TaskTable) { long("timestamp").clientDefault { Clock.System.now().toEpochMilliseconds() } }

    var TaskEntity.timestamp by timestampCol

    init {
        transaction(db) {
            SchemaUtils.create(TaskTable)
        }
        EntityHook.subscribe { change ->
            val changedEntity = change.toEntity(TaskEntity) ?: return@subscribe
            if (change.changeType == EntityChangeType.Updated || change.changeType == EntityChangeType.Created) {
                changedEntity.timestamp = Clock.System.now().toEpochMilliseconds()
                println("Changed task $changedEntity at ${Clock.System.now()}")
            }
        }
    }

    fun apply(mutator: Mutator) = mutator.mutate()

    fun apply(mutators: List<Mutator>) = mutators.forEach {
        apply(it)
    }

    @OptIn(ExperimentalUuidApi::class)
    fun getUpdatedSince(timestamp: Long): SyncResult.Updates = transaction(db) {
        val updates = TaskTable.selectAll()
            .where { timestampCol greaterEq timestamp }
            .map {
                RowDiff(
                    it[TaskTable.id].value.toKotlinUuid(),
                    Task(it[TaskTable.name], it[TaskTable.done])
                )
            }
            .toList()
        val max = timestampCol.max()
        val lastUpdate: Long = TaskTable.select(max)
            .singleOrNull()
            ?.get(max)
            ?: timestamp
        SyncResult.Updates(
            updates,
            lastUpdate
        )
    }
}
