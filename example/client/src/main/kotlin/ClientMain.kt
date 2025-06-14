import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import me.dvyy.syncengine.common.ClientDataStore
import me.dvyy.syncengine.common.SyncClient
import me.dvyy.syncengine.common.initDatabase
import me.dvyy.syncengine.common.mutators.MutatorQueue
import me.dvyy.syncengine.common.ui.Task
import me.dvyy.syncengine.common.ui.TaskTable
import me.dvyy.syncengine.ui.TasksViewModel

suspend fun main() {
    coroutineScope {
        initDatabase()
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

@Composable
fun MainApp(viewModel: TasksViewModel = viewModel()) {
    val store = remember { ClientDataStore(TaskTable.diff, MutatorQueue(), CoroutineScope(Dispatchers.IO)) }
    val syncClient = remember { SyncClient(store) }
    val scope = rememberCoroutineScope()
    Scaffold {
        val tasks by viewModel.allTasks.collectAsState()
        LazyColumn {
            item {
                Row {
                    Button(onClick = { viewModel.tasks.new(Task("new", false)) }) {
                        Text("Add task (total ${viewModel.count.collectAsState(-1).value})")
                    }
                    Button(onClick = { scope.launch { runCatching { syncClient.sync() }.onFailure { it.printStackTrace() } } }) {
                        Text("Sync")
                    }
                }
            }
            items(tasks, key = { it }) { taskId ->
                val task by viewModel.tasks.observe(taskId)
                TextField(task?.name ?: "", onValueChange = { new ->
                    println("Storing $new")
                    viewModel.tasks.mutate(taskId) { it.copy(name = new) }
                })
            }
        }
    }
}
