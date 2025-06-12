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
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import me.dvyy.syncengine.common.initDatabase
import me.dvyy.syncengine.common.ui.Task
import me.dvyy.syncengine.startSyncServer
import me.dvyy.syncengine.ui.TasksViewModel

suspend fun main() {
    coroutineScope {
        launch(Dispatchers.IO) {
            startSyncServer()
        }
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
//    val store = remember { ClientDataStore(ReversibleDataStore(), CoroutineScope(Dispatchers.IO)) }
//    val syncClient = remember { SyncClient(store) }
    Scaffold {
        val tasks by viewModel.allTasks.collectAsState()
        LazyColumn {
            item {
                Button(onClick = { viewModel.tasks.new(Task("new", false)) }) {
                    Text("Add task (total ${viewModel.count.collectAsState(-1).value})")
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
