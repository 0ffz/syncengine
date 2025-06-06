package me.dvyy.syncengine.common

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.Table.Dual.default
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.r2dbc.*
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import kotlin.time.Duration.Companion.seconds


private class KeyValueTable(name: String) : LongIdTable(name = name) {
    val value = text("value")
    val editTime = long("timestamp")//datetime("timestamp").defaultExpression(CurrentDateTime)
}

//private class KeyValueEntity(
//    table: KeyValueTable,
////    mergedView: KeyValueTable,
//    id: EntityID<Long>,
//) : LongEntity(id) {
//    var value by table.value
//    var timestamp by table.editTime
//}

class DiffableTables<T : IdTable<*>>(
    val underlying: T,
    val overlay: T,
    val view: T,
) {
    suspend fun rollback() {
        overlay.deleteAll()
    }
}

suspend fun <T : IdTable<*>> R2dbcTransaction.diffableTable(
    name: String,
    constructor: (String) -> T,
): DiffableTables<T> {
    val underlying = constructor(name)
    val overlay = constructor("${name}_overlay")
    val overlayRemovedField = overlay.bool("removed").default(false)
    SchemaUtils.create(underlying, overlay)
    val mergedView = underlying.leftJoin(overlay, onColumn = { underlying.id }, otherColumn = { overlay.id })
        .select(
            underlying.columns.zip(overlay.columns).map { (it, other) ->
                if (it == underlying.id) it else Coalesce(other, it).alias("\"${it.name}\"")
            })
        .where { overlayRemovedField.isDistinctFrom(booleanLiteral(true)) }
        .unionAll(
            overlay.select(overlay.columns.minus(overlayRemovedField))
                .where {
                    notExists(
                        underlying.select(byteLiteral(1)).where(underlying.id eq overlay.id)
                    ).and { overlayRemovedField.isDistinctFrom(booleanLiteral(true)) }
                }
        )
        .prepareSQL(this).also { println(it) }
    val viewName = "${name}_merged"
    exec("CREATE OR REPLACE VIEW $viewName AS $mergedView")
//    exec("""
//        CREATE TRIGGER ${viewName}_redirect
//        INSTEAD OF INSERT, UPDATE, DELETE
//        ON $viewName
//        FOR EACH ROW CALL "TODO";
//    """.trimIndent())
    return DiffableTables(underlying, overlay, constructor("${name}_merged"))
}

@OptIn(FlowPreview::class)
suspend fun main() {
//    val jdbcUrl = "jdbc:sqlite:mydatabase.db?journal_mode=WAL&synchronous=OFF&journal_size_limit=500"
//
    R2dbcDatabase.connect("r2dbc:h2:file:///./test;DB_CLOSE_DELAY=-1;")
    R2dbcTransaction.globalInterceptors.add(CustomInterceptor)
    val tables = suspendTransaction { diffableTable("keyvalue", ::KeyValueTable) }
    val scope = CoroutineScope(Dispatchers.IO).launch {
        tables.overlay.selectAll().observe { toList() }.collect {
            println("Updated!!$it")
        }
    }
    delay(1.seconds)
    suspendTransaction {
//        registerInterceptor(CustomInterceptor)
//        underlying.insert { it[value] = "value" }
//        underlying.insert { it[value] = "value2" }
        tables.overlay.insert { it[value] = "hello"; it[editTime] = 1 }
//        println(tables.view.selectAll().toList())
    }
    delay(1.seconds)
    scope.cancel()
}

fun interface QueryObserver {
    fun notifyUpdate()
}

inline fun <T> Query.observe(crossinline collect: suspend Query.() -> T): Flow<T> = channelFlow {
    val channel = Channel<Unit>(CONFLATED)
    channel.trySend(Unit)
    val observer = QueryObserver { channel.trySend(Unit) }
    println("Adding to $targets")
    targets.forEach {
        CustomInterceptor.listeners.getOrPut(it.tableName) { mutableSetOf() }.add(observer)
    }

    try {
        for (unit in channel) {
            suspendTransaction(Dispatchers.IO) { send(this@observe.collect()) }
        }
    } finally {
        println("removing from $targets")
        targets.forEach {
            CustomInterceptor.listeners[it.tableName]?.remove(observer)
        }
    }
}

