import androidx.compose.foundation.layout.Column
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.dvyy.syncengine.common.ClientDataStore
import me.dvyy.syncengine.common.ReversibleDataStore
import me.dvyy.syncengine.common.SyncClient
import me.dvyy.syncengine.common.mutators.MutateText
import me.dvyy.syncengine.startSyncServer

suspend fun main() {
    coroutineScope {
        launch(Dispatchers.IO) {
            startSyncServer()
        }
    application {
        Window(onCloseRequest = ::exitApplication) {
            MainApp()
        }
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
//    LaunchedEffect(Unit) {
//        repeat(100) {
//            store.incrementCounter()
//            delay(100)
//        }
//    }
    Scaffold {
        val counter by store.observe(1).collectAsState("0")
        val text by store.observe(2).collectAsState("")
//        var text by remember { mutableStateOf("") }
        Column {
            Button(onClick = { store.incrementCounter() }) {
                Text("Increment: $counter")
            }
            TextField(text ?: "", onValueChange = {
                println("Storing $it")
                store.invoke(MutateText(row = 2, insertAfter = 0, text = it))
            })
        }
    }
}
