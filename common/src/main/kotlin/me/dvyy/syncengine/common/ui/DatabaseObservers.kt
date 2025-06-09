package me.dvyy.syncengine.common.ui

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.dvyy.syncengine.common.observe
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.*

fun interface QueryObserver {
    fun notifyUpdate()
}

class MappedTable<out T : Table, U>(val table: T, val mapper: (U) -> T) {

}

abstract class EntityMapper<T>(val table: UUIDTable) {
    private val flows = mutableMapOf<UUID, MutableStateFlow<T?>>()
//    private val uuidToId = mutableMapOf<UUID, Int>()

    //    fun get(uuid: UUID): T? {
//        return flows[uuid]?.value
//    }
    val dbScope = CoroutineScope(Dispatchers.IO)

    abstract fun map(row: ResultRow): T
    abstract fun insert(entity: T, row: UpdateBuilder<*>)

    fun update(uuid: UUID, value: T) {
        flows[uuid]?.update { value }
        dbScope.launch {
            transaction {
                table.update(where = { table.id.eq(uuid) }) { insert(value, it) }
            }
        }
    }

    fun update(uuid: UUID, modify: (T?) -> T) {
        val uiCache = flows[uuid]
        uiCache?.update { modify(it) }

//        dbScope.launch {
//            suspendTransaction {
//
//            }
//        }
    }

    fun observe(uuid: UUID): Flow<T?> = flows.getOrPut(uuid) { //TODO synchronized
        val flow = MutableStateFlow<T?>(null)
        dbScope.launch {
            transaction {
                flow.update {
                    map(table.selectAll().where { table.id eq uuid }.single())
                }
            }
        }
        flow
    }
}

fun <T> launchTransaction(run: JdbcTransaction.() -> T): Deferred<T> = CoroutineScope(Dispatchers.IO).async {
    transaction { run() }
}

object TaskRepo {
    val tasks: Flow<List<UUID>> = TaskEntity.observe {
        all().orderBy(TaskTable.name to SortOrder.DESC)
            .map { it.id.value }
            .toList()
    }


//        .observe { map { it[Tasks.id].value }.toList() }

    fun new() = launchTransaction {
        TaskEntity.new { name = "new"; done = false; }
    }
//    fun new() = CoroutineScope(Dispatchers.IO).launch {
//        transaction {
//            TaskEntity.new {
//                name = "new"
//                done = false
//            }
//        }
//    }
}


data class Task(
    val name: String,
    val done: Boolean,
) {
//    companion object : EntityMapper<Task>(Tasks) {
//        override fun map(row: ResultRow): Task = Tasks.run { Task(row[name], row[done]) }
//        override fun insert(entity: Task, row: UpdateBuilder<*>) = Tasks.run {
//            row[name] = entity.name
//            row[done] = entity.done
//        }
//    }
}

object TaskTable : UUIDTable() {
    val name = varchar("name", 255)
    val done = bool("done")
}

class TaskEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TaskEntity>(TaskTable)

    var name by TaskTable.name
    var done by TaskTable.done
}