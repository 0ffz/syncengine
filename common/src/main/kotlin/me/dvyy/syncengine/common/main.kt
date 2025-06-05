package me.dvyy.syncengine.common

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.toList
import org.h2.mvstore.MVStore
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.core.statements.GlobalStatementInterceptor
import org.jetbrains.exposed.v1.dao.View
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.sqlite.SQLiteDataSource

private class KeyValueTable(name: String) : LongIdTable(name = name) {
    val value = text("value")
    val editTime = long("timestamp")//datetime("timestamp").defaultExpression(CurrentDateTime)
}

@OptIn(FlowPreview::class)
suspend fun main() {
//    val jdbcUrl = "jdbc:sqlite:mydatabase.db?journal_mode=WAL&synchronous=OFF&journal_size_limit=500"
//
    val db = R2dbcDatabase.connect("r2dbc:h2:file:///regular;DB_CLOSE_DELAY=-1;")
    val underlying = KeyValueTable("underlying_table")
    val overlay = KeyValueTable("overlay")
//    val downloaded = KeyValueTable("downloaded")
    suspendTransaction {
        SchemaUtils.create(underlying, overlay)
        return@suspendTransaction
        exec("""
            CREATE VIEW merged_view AS
            SELECT
                u.id,
                COALESCE(o.value, u.value) AS value,
                COALESCE(o.timestamp, u.timestamp) AS timestamp
            FROM
                underlying_table u
            LEFT JOIN
                overlay o ON u.id = o.id;
        """.trimIndent())
        underlying.insert { it[value] = "value" }
        underlying.insert { it[value] = "value2" }
        overlay.insert { it[value] = "override!" }
        val merged = KeyValueTable("merged_view")
        println(merged.selectAll().toList())
    }
//    val database = R2dbcDatabase.connect(jdbcUrl, "org.sqlite.JDBC")
//    val server = ServerDataStore()
//    val clientServerTransport = ClientServerTransport()
//    val serverClientTransport = ServerClientTransport()
//    val syncService = SyncServiceImpl(Job(), server)


//    while (true) {
//        server.apply(serverClientTransport.receive())
//        server.store.forEach { (row, value) ->
//            serverClientTransport.send(RowDiff(row, value))
//        }
//    }
//    val service: TestService = client.withService<TestService>()
//    val server = Mutators(serverClientTransport)
//    server.registerService<TestService> { ctx -> TestServiceImpl(ctx) }
//    println(service.test(1))
}
