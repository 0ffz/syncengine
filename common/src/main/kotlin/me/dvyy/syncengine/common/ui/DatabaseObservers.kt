package me.dvyy.syncengine.common.ui

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import me.dvyy.syncengine.common.observe
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.dao.ImmutableCachedEntityClass
import org.jetbrains.exposed.v1.dao.ImmutableEntityClass
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.json.json
import org.jetbrains.exposed.v1.json.jsonb
import java.util.*
import kotlin.uuid.Uuid

fun interface QueryObserver {
    fun notifyUpdate()
}

private val dbScope = CoroutineScope(Dispatchers.IO.limitedParallelism(1))

fun <T> launchTransaction(run: JdbcTransaction.() -> T): Deferred<T> = dbScope.async {
    transaction { run() }
}

data class Task(
    val name: String,
    val done: Boolean,
)

object TaskTable : UUIDTable() {
    val name = varchar("name", 255)
    val done = bool("done")
}

object ListTable: UUIDTable() {
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