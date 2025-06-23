package me.dvyy.syncengine.db

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteStatement
import me.dvyy.syncengine.db.tables.Table
import me.dvyy.syncengine.db.tables.TableReading
import org.intellij.lang.annotations.Language
import kotlin.coroutines.RestrictsSuspension

@RestrictsSuspension
class WriteTransaction(connection: SQLiteConnection) : Transaction(connection) {
    val modifiedTables = mutableSetOf<Table>()

    fun modified(vararg tables: TableReading) {
        modifiedTables.addAll(TableReading.reduce(tables.toSet()))
    }
}

@RestrictsSuspension
open class Transaction(
    @PublishedApi
    @JvmField
    internal val connection: SQLiteConnection,
) {
    inline fun <T> prepare(
        @Language("SQLite") sql: String,
        statement: SQLiteStatement.() -> T,
    ): T {
        return connection.prepare(sql).use {
            statement(it)
        }
    }

    inline fun SQLiteStatement.bindParams(vararg params: Any) {
        params.forEachIndexed { i, it -> bindAny(i + 1, it) }
    }

    inline fun <T> getSingle(
        @Language("SQLite") sql: String,
        vararg parameters: Any,
        statement: SQLiteStatement.() -> T,
    ): T {
        return prepare(sql) {
            bindParams(*parameters)
            step()
            statement()
        }
    }

    inline fun <T> getList(@Language("SQLite") sql: String, statement: SQLiteStatement.() -> T): List<T> = buildList {
        prepare(sql) {
            while (step()) {
                add(statement())
            }
        }
    }

    inline fun <T> forEach(@Language("SQLite") sql: String, statement: SQLiteStatement.() -> T) {
        prepare(sql) {
            while (step()) {
                statement()
            }
        }
    }

    fun exec(@Language("SQLite") sql: String, vararg parameters: Any) {
        prepare(sql) {
            bindParams(*parameters)
            step()
        }
    }
}
