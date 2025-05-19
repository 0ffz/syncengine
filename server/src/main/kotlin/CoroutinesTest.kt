import kotlinx.coroutines.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlin.time.Duration.Companion.seconds

suspend fun main() {
    val coldFlow = flow<Int> {
        println("Started")
        try {
            repeat(100) {
                println("Emitting: $it")
                emit(it)
                delay(1.seconds)
            }
        } finally {
            println("Closed")
        }
    }.shareIn(CoroutineScope(Dispatchers.IO), started = SharingStarted.WhileSubscribed())

    coroutineScope {
        val job = launch {
            coldFlow.collect { println("[1] Collected: $it") }
        }
//        launch {
//            coldFlow.collect { println("[2] Collected: $it") }
//        }

        delay(2.seconds)
        job.cancel()
        delay(1.seconds)
        launch {
            coldFlow.collect { println("[3] Collected: $it") }
        }
    }.join()
}
