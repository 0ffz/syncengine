package me.dvyy.syncengine.common.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update
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
            suspendTransaction {
                table.update(where = { table.id.eq(uuid) }) { insert(value, it) }
            }
        }
    }

    fun observe(uuid: UUID): Flow<T?> = flows.getOrPut(uuid) { //TODO synchronized
        val flow = MutableStateFlow<T?>(null)
        dbScope.launch { suspendTransaction { flow.update { map(table.selectAll().where { table.id eq uuid }.single()) } } }
        flow
    }
}

data class Task(val name: String, val done: Boolean) {
    companion object : EntityMapper<Task>(Tasks) {
        override fun map(row: ResultRow): Task = Tasks.run { Task(row[name], row[done]) }
        override fun insert(entity: Task, row: UpdateBuilder<*>) = Tasks.run {
            row[name] = entity.name
            row[done] = entity.done
        }
    }
}

object Tasks : UUIDTable() {
    val name = varchar("name", 255)
    val done = bool("done")
}

suspend fun main() {
}