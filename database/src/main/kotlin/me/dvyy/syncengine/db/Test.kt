package me.dvyy.syncengine.db

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.dvyy.syncengine.db.tables.*
import java.util.*
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
suspend fun main() {
    createSchema(
        tables = listOf(JsonTable, MutatorsTable, SubtaskRelation),
        views = listOf(TasksView)
    )
//    CoroutineScope(Dispatchers.IO).launch {
//        Database.watch(TasksView) { getList("SELECT name FROM $TasksView") { getText(0) } }.collect {
//            println("Got: $it")
//        }
//    }
//    repeat(100) {
//        Database.read {
//            measureTime {
//                buildList {
//                    prepare("SELECT name FROM task") {
//                        while (step()) {
//                            add(getText(0))
//                        }
//                    }
//                }
//            }.let { println(it) }
//        }
//    }
    delay(1000)
    val tasks = JsonTableDAO(Task.serializer(), JsonTable)
    val subtaskRelation = RelationTableDAO<Task>()

    val id = UUID.randomUUID()
//    CoroutineScope(Dispatchers.IO).launch {
//        Database.read {
//            subtaskRelation.relatedTo(id)
//        }
//    }

//    Database.write {
//        repeat(100) {
//            tasks.mutate(UUID.randomUUID(), Task("task $it"))
//        }
//    }
//    repeat(100) {
//        CoroutineScope(Dispatchers.IO).launch {
//            Database.read {
//                println(getList("SELECT name FROM $TasksView") { getText(0) })
//            }
//        }
//
//    }
//    repeat(100) {
//        Database.write {
//            val first = prepare("SELECT id FROM jsondata LIMIT 1") { step(); getBlob(0) }
//            exec("DELETE FROM jsondata WHERE id = ?", first)
////            tasks.mutate(id, Task("hey world", false))
////            println(tasks.get(id))
//        }
//    }
//    delay(1000)
}

// FROM powersync-kmp client
