package me.dvyy.syncengine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.dvyy.syncengine.common.CustomInterceptor
import me.dvyy.syncengine.common.observe
import me.dvyy.syncengine.common.tables.TaskEntity
import me.dvyy.syncengine.common.tables.TaskTable
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.Test

class ObserverTests {
    val db = Database.connect("jdbc:sqlite::temp.db:")

    @Test
    fun `observer should trigger on table update`() = runBlocking{
        JdbcTransaction.globalInterceptors.add(CustomInterceptor)
        transaction {
            SchemaUtils.create(TaskTable)
        }
        val observer = TaskEntity.observe {
            TaskEntity.all().toList()
        }
        launch(Dispatchers.IO) {
            observer.collect { println(it) }
        }
        val new = transaction {
            TaskEntity.new { name = "test"; done = false }
        }
//        println(new.name)
        //                observer.collect {
        //                    println(it)
        //                }
//            runBlocking {
//            }
    }
}
