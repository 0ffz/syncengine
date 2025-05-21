import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

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
    Scaffold {
        var text by remember { mutableStateOf("") }
        TextField(text, onValueChange = { text = it })
    }
}
