package me.dvyy.syncengine.common.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.serialization.Serializable
import me.dvyy.syncengine.common.DiffableTables
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.*
import kotlin.time.measureTimedValue

fun interface QueryObserver {
    fun notifyUpdate()
}

private val dbScope = CoroutineScope(Dispatchers.IO.limitedParallelism(1))

fun <T> launchTransaction(run: JdbcTransaction.() -> T): Deferred<T> = dbScope.async {
    measureTimedValue {
        transaction { run() }
    }.also { println("WRITE took ${it.duration}") }.value
}

@Serializable
data class Task(
    val name: String,
    val done: Boolean,
)

open class TaskTable(name: String) : UUIDTable(name) {
    val name = varchar("name", 255)
    val done = bool("done")

    companion object : TaskTable("task") {
        val diff = DiffableTables(this, ::TaskTable)
    }
}

object ListTable : UUIDTable() {
    val name = varchar("name", 255)
}

object ListItemsTable : IntIdTable() {
    val list = reference("list", ListTable)
    val task = reference("task", TaskTable)
    val order = text("order")
}

class TaskEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TaskEntity>(TaskTable)

    var name by TaskTable.name
    var done by TaskTable.done
}

class ListEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ListEntity>(ListTable)

    var name by ListTable.name
    var tasks by TaskEntity via ListItemsTable //orderBy ListItemsTable.order
}

class ListItemEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ListItemEntity>(ListItemsTable)

    var list by ListEntity referencedOn ListItemsTable.list
    var task by TaskEntity referencedOn ListItemsTable.task
    var order by ListItemsTable.order
}