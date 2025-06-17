package me.dvyy.syncengine.common

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import me.dvyy.syncengine.common.mutators.MutatorsTable
import me.dvyy.syncengine.common.ui.QueryObserver
import me.dvyy.syncengine.common.ui.TaskTable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.statements.StatementType
import org.jetbrains.exposed.v1.dao.EntityClass
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.sqlite.SQLiteConfig


suspend fun initDatabase(path: String = "test.db") {
//    Database.connect("r2dbc:h2:file:///./test;DB_CLOSE_DELAY=-1;")
    val db = Database.connect(
        "jdbc:sqlite:$path",
        setupConnection = { conn ->
            val sqliteConfig = SQLiteConfig().apply {
                //https://www.powersync.com/blog/sqlite-optimizations-for-ultra-high-performance
                setJournalMode(SQLiteConfig.JournalMode.WAL)
                setSynchronous(SQLiteConfig.SynchronousMode.NORMAL)
                setJournalSizeLimit(6144000)
            }
            sqliteConfig.apply(conn)
        }
    )
    JdbcTransaction.globalInterceptors.add(CustomInterceptor)
    transaction {
        with(TaskTable.diff) { initialize() }
        SchemaUtils.create(MutatorsTable)
//        SchemaUtils.create(TaskTable, ListTable, ListItemsTable, MutatorsTable)
        exec("PRAGMA journal_mode;", explicitStatementType = StatementType.PRAGMA) {
            it.next()
            println("Mode set to ${it.getString(1)}") // Mode set to wal
        }
//        MutatorQueue.insert { it[mutator] = Increment(1, 2) }
    }
}

@OptIn(FlowPreview::class)
suspend fun main() {
    initDatabase()
//    val tasks = transaction {
//        val task = TaskEntity.new { name = "Test Task"; done = false }
//        val list = ListEntity.new { name = "Test List"; }
//        ListItemEntity.new { this.list = list; this.task = task; order = "a" }
//        ListEntity.get(list.id).tasks.mapLazy { it.name }.toList()
//    }
//    println(tasks)
//    return
    transaction { with(TaskTable.diff) { initialize() } }
//    val scope = CoroutineScope(Dispatchers.IO).launch {
//        tables.overlay.selectAll().observe { toList() }.collect {
//            println("Updated!!$it")
//        }
//    }
    transaction {
//        registerInterceptor(CustomInterceptor)
//        underlying.insert { it[value] = "value" }
//        underlying.insert { it[value] = "value2" }
//        val test = tables.underlying.insert { it[value] = "hello"; it[editTime] = 1 }
//        tables.view.update(where = {tables.view.id eq 1 }) { it[editTime] = 100 }
//        tables.view.deleteAll()
//        println(TaskTable.selectAll().map { it[TaskTable.name] })
        TaskTable.diff.rollback()
    }
}

inline fun <T, C : EntityClass<*, *>> C.observe(crossinline collect: C.() -> T): Flow<T> = observe(listOf(table)) {
    collect()
}

inline fun <T> Query.observe(crossinline collect: Query.() -> T): Flow<T> = observe(targets) {
    collect()
}

inline fun <T> observe(targets: List<Table>, crossinline collect: Transaction.() -> T): Flow<T> = channelFlow {
    println("Adding to $targets")
    val channel = Channel<Unit>(CONFLATED)
    channel.trySend(Unit)
    val observer = QueryObserver { channel.trySend(Unit) }
    targets.forEach {
        CustomInterceptor.listeners.getOrPut(it.tableName) { mutableSetOf() }.add(observer)
    }

    try {
        for (unit in channel) {
            launch(Dispatchers.IO) { send(transaction { (collect()) }) }
        }
    } finally {
        println("removing from $targets")
        targets.forEach {
            CustomInterceptor.listeners[it.tableName]?.remove(observer)
        }
    }
}

