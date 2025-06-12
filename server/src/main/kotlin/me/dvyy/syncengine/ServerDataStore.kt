@file:OptIn(ExperimentalTime::class)

package me.dvyy.syncengine

import me.dvyy.syncengine.common.RowDiff
import me.dvyy.syncengine.common.SyncResult
import me.dvyy.syncengine.common.mutators.Mutator
import me.dvyy.syncengine.common.ui.Task
import me.dvyy.syncengine.common.ui.TaskTable
import org.jetbrains.exposed.v1.core.max
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
    val timestampCol = TaskTable.long("timestamp")

    init {
        transaction {
            SchemaUtils.create(TaskTable)
        }
    }

    fun apply(mutator: Mutator) = mutator.mutate()

    fun apply(mutators: List<Mutator>) = mutators.forEach {
        apply(it)
    }

    @OptIn(ExperimentalUuidApi::class)
    fun getUpdatedSince(timestamp: Long): SyncResult.Updates = transaction {
        val updates = TaskTable.selectAll()
            .where { timestampCol greater timestamp }
            .map {
                RowDiff(
                    it[TaskTable.id].value.toKotlinUuid(),
                    Task(it[TaskTable.name], it[TaskTable.done])
                )
            }
            .toList()
        val lastUpdate = TaskTable.select(timestampCol.max())
            .singleOrNull()
            ?.get(timestampCol)
            ?: timestamp
        SyncResult.Updates(
            updates,
            lastUpdate
        )
    }
}
