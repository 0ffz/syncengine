import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import me.dvyy.syncengine.common.ClientDataStore
import me.dvyy.syncengine.common.ReversibleDataStore
import me.dvyy.syncengine.common.SyncClient

fun main() {
    application {
        Window(onCloseRequest = ::exitApplication) {
            MainApp()
        }
        Window(onCloseRequest = ::exitApplication) {
            MainApp()
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
//        var text by remember { mutableStateOf("") }
        Button(onClick = { store.incrementCounter() }) {
            Text("Increment: $counter")
        }
//        TextField(text, onValueChange = { text = it })
    }
}
