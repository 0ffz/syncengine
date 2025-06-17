import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import me.dvyy.syncengine.common.initDatabase


suspend fun main() {
    initDatabase("test2.db")
    application {
        Window(onCloseRequest = ::exitApplication) {
            MainApp()
        }
    }
}