package me.dvyy.syncengine.db

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.*
import androidx.sqlite.execSQL
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import me.dvyy.syncengine.db.tables.TableReading
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

//TODO handle separate connections for async reads and single write
open class Database(
    private val driver: BundledSQLiteDriver,
    private val readConnections: Int = 4,
    private val path: String,
    val watchQueryThrottle: Duration = 100.milliseconds
) {
    val writeConnection = createConnection(readOnly = false)
    val readerConnectionPool = Channel<Lazy<SQLiteConnection>>(readConnections)
    val dbWriteDispatcher = newSingleThreadContext("db-writes")
    val dbReadDispatcher = newFixedThreadPoolContext(readConnections, "db-reads")
    val observers = DatabaseObservers()

    fun createConnection(readOnly: Boolean): SQLiteConnection {
        //FIXME readonly errors
        val readFlag = if (readOnly) SQLITE_OPEN_READONLY else SQLITE_OPEN_READWRITE
        return LoggingConnection(
            driver.open(
                path,
                SQLITE_OPEN_READWRITE or SQLITE_OPEN_CREATE or SQLITE_OPEN_NOMUTEX
            )
        ).also {
            it.execSQL(
                """
                PRAGMA journal_mode=WAL;
                PRAGMA synchronous=normal;
                PRAGMA journal_size_limit=6144000;
                """.trimIndent()
            )
        }
    }

    init {
        repeat(readConnections) {
            readerConnectionPool.trySend(
                lazy { createConnection(readOnly = true) }
            )
        }
    }

    // TODO need SupervisorJob? Check this is safe with parallel writes
    suspend inline fun <T> write(
        crossinline block: WriteTransaction.() -> T,
    ): T = withContext(dbWriteDispatcher) {
        val tx = WriteTransaction(writeConnection)
        writeConnection.transaction {
            tx.block()
        }.also { observers.notify(tx.modifiedTables) }
    }

    suspend inline fun <T> read(
        crossinline block: Transaction.() -> T,
    ): T {
        val conn = readerConnectionPool.receive().value
        try {
            return withContext(dbReadDispatcher) {
                Transaction(conn).block()
            }
        } finally {
            readerConnectionPool.send(lazy { conn })
        }
    }

    inline fun <T> watch(
        vararg tables: TableReading,
        crossinline read: Transaction.() -> T,
    ) = flow {
        emit(Database.read { read() })
        observers.forTables(TableReading.reduce(tables.toSet())).throttle(watchQueryThrottle).collect {
            emit(Database.read { read() })
        }
    }

    companion object : Database(
        driver = BundledSQLiteDriver(),
        path = "/var/home/offz/projects/syncengine/test.db"
    )
}
