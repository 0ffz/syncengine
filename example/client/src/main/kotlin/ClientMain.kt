import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import me.dvyy.syncengine.common.*
import me.dvyy.syncengine.common.ui.Task
import me.dvyy.syncengine.common.ui.TaskEntity
import me.dvyy.syncengine.common.ui.TaskRepo
import java.util.*

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

@Composable
fun MainApp() {
    val store = remember { ClientDataStore(ReversibleDataStore(), CoroutineScope(Dispatchers.IO)) }
    val syncClient = remember { SyncClient(store) }
    Scaffold {
        val tasks by TaskRepo.tasks.collectAsState(initial = emptyList())
        LazyColumn {
            item {
                Button(onClick = { TaskRepo.new() }) {
                    Text("Add task")
                }
            }
            items(tasks, key = { it }) { taskId ->
                val task by remember { TaskRepo.observe(taskId) }.collectAsState(initial = null)
                TextField(task?.name ?: "", onValueChange = { new ->
                    println("Storing $new")
                    TaskEntity.findByIdAndUpdate(taskId) { it.name = new }
                    Task.update(taskId, (task ?: Task("", false)).copy(name = it))
//                store.invoke(MutateText(row = 2, insertAfter = 0, text = it))
                })
            }
        }
    }
}
