package me.dvyy.syncengine.db

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import me.dvyy.syncengine.db.tables.*
import java.util.*
import kotlin.time.measureTime
import kotlin.uuid.ExperimentalUuidApi

suspend fun createSchema(tables: List<Table>, views: List<View>) = Database.write {
    tables.forEach { exec(it.createStatement) }
    views.forEach { exec("CREATE VIEW IF NOT EXISTS ${it::class.simpleName} AS ${it.selectStatement}") }
}

@OptIn(ExperimentalUuidApi::class)
suspend fun main() {
    createSchema(
        tables = listOf(JsonTable, MutatorsTable, SubtaskRelation),
        views = listOf(TasksView)
    )
    repeat(100) {
        Database.read {
            measureTime {
                buildList {
                    prepare("SELECT name FROM task") {
                        while (step()) {
                            add(getText(0))
                        }
                    }
                }
            }.let { println(it) }
        }
    }
    val tasks = JsonTableDAO(Task.serializer(), "jsondata")
    val subtaskRelation = RelationTableDAO<Task>()

    val id = UUID.randomUUID()
    CoroutineScope(Dispatchers.IO).launch {
        Database.read {
            subtaskRelation.relatedTo(id)
            delay(100)
        }
    }
    Database.write {
        tasks.mutate(id, Task("hey world", false))
        println(tasks.get(id))
    }
}

@Serializable
data class Task(val name: String, val done: Boolean = false)
