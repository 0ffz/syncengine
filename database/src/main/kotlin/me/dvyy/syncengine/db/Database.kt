package me.dvyy.syncengine.db

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.*
import androidx.sqlite.execSQL
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import kotlin.coroutines.RestrictsSuspension

//TODO handle separate connections for async reads and single write
open class Database(
    private val driver: BundledSQLiteDriver,
    private val readConnections: Int = 4,
    private val path: String,
) {
    val writeConnection = createConnection(readOnly = false)
    val readerConnectionPool = Channel<Lazy<SQLiteConnection>>(readConnections)
    val dbWriteDispatcher = newSingleThreadContext("db-writes")
    val dbReadDispatcher = newFixedThreadPoolContext(readConnections, "db-reads")

    fun createConnection(readOnly: Boolean): SQLiteConnection {
        //FIXME readonly errors
        val readFlag = if (readOnly) SQLITE_OPEN_READONLY else SQLITE_OPEN_READWRITE
        return driver.open(path, SQLITE_OPEN_READWRITE or SQLITE_OPEN_CREATE or SQLITE_OPEN_NOMUTEX).also {
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
        writeConnection.transaction {
            WriteTransaction(writeConnection).block()
        }
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

    companion object : Database(
        driver = BundledSQLiteDriver(),
        path = "test.db"
    )
}

