package me.dvyy.syncengine.server.schema

import co.touchlab.kermit.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.uuid.Uuid

@OptIn(ExperimentalAtomicApi::class)
class Reference<T> {
    val refCount = AtomicInt(0)
    fun initialize() {}
}

@OptIn(ExperimentalAtomicApi::class)
class ItemPool<R>(
//    directory: Path,
    val initialize: suspend (Uuid) -> R,
    val onClose: (R) -> Unit,
    val stopTimeoutMillis: Long,
    val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) {
    val cacheMap = ConcurrentHashMap<Uuid, SharedFlow<R>>()

    suspend inline fun <T> use(id: Uuid, crossinline use: suspend (R) -> T): T {
        return cacheMap.getOrPut(id) {
            var flowInstance: SharedFlow<R>? = null
            flow {
                val item = initialize(id)
                try {
                    emit(item)
                    awaitCancellation()
                } finally {
                    onClose(item)
                }
            }.onCompletion {
                Logger.d { "Closing workspace database $id" }
                cacheMap.remove(id, flowInstance)
            }
                .shareIn(
                    scope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis, replayExpirationMillis = 0),
                    replay = 1
                )
                .also { flowInstance = it }
        }.map { use(it) }.first()
    }
}

suspend fun main() {
    val pool = ItemPool<String>({ "Random string ${Random.nextInt(0..10000)}!" }, { println("Closed $it") }, 1000)
    val id = Uuid.random()
    withContext(Dispatchers.IO) {
        launch {
            pool.use(id) {
                println("Got $it")
                delay(3000)
            }
        }
        launch {
            pool.use(id) {
                println("Got $it")
                delay(2000)
            }
        }
    }
    println("Exited!")
    delay(2000)
    pool.use(id) {
        println("Got $it")
        delay(2000)
    }
    delay(2000)
    println(pool.cacheMap.keys.toList())
}