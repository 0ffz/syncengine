import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import me.dvyy.syncengine.common.ClientDataStore
import me.dvyy.syncengine.common.ReversibleDataStore
import me.dvyy.syncengine.common.SyncClient
import me.dvyy.syncengine.common.initDatabase
import me.dvyy.syncengine.common.mutators.MutateText
import me.dvyy.syncengine.common.observe
import me.dvyy.syncengine.common.ui.Task
import me.dvyy.syncengine.common.ui.Tasks
import me.dvyy.syncengine.startSyncServer
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import java.util.UUID

suspend fun main() {
    initDatabase()
    coroutineScope {
//        launch(Dispatchers.IO) {
//            startSyncServer()
//        }
        application {
//            Window(onCloseRequest = ::exitApplication) {
//                MainApp()
//            }
            Window(onCloseRequest = ::exitApplication) {
                MainApp()
            }
        }
    }
}

object TaskRepo {
    val tasks: Flow<List<UUID>> = Tasks.select(Tasks.id)
        .orderBy(Tasks.name to SortOrder.DESC)
        .observe { map { it[Tasks.id].value }.toList() }

    fun new() = CoroutineScope(Dispatchers.IO).launch {
        suspendTransaction {
            Tasks.insert { it[Tasks.id] = UUID.randomUUID(); it[Tasks.name] = ""; it[Tasks.done] = false; }
        }
    }
}
@Composable
fun MainApp() {
    val store = remember { ClientDataStore(ReversibleDataStore(), CoroutineScope(Dispatchers.IO)) }
    val syncClient = remember { SyncClient(store) }
    Scaffold {
        val tasks by TaskRepo.tasks.collectAsState(initial = emptyList())
//        val task by Task.observe(id).collectAsState(null)
//        val counter by store.observe(1).collectAsState("0")
//        val text by store.observe(2).collectAsState("")
////        var text by remember { mutableStateOf("") }
        LazyColumn {
            item {
                Button(onClick = { TaskRepo.new() }) {
                    Text("Add task")
                }
            }
            items(tasks, key = { it }) { taskId ->
                val task by remember { Task.observe(taskId) }.collectAsState(initial = null)
                TextField(task?.name ?: "", onValueChange = {
                    println("Storing $it")
                    Task.update(taskId, (task ?: Task("", false)).copy(name = it))
//                store.invoke(MutateText(row = 2, insertAfter = 0, text = it))
                })
            }
        }
    }
}
