package me.dvyy.syncengine.shared

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.rpc.RemoteService
import kotlinx.rpc.annotations.Rpc
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import me.dvyy.syncengine.jsondiff.jsonMerge
import me.dvyy.syncengine.jsondiff.jsonSubtract
import kotlin.coroutines.CoroutineContext

fun interface Mutator<Params> {
    val name: String get() = this::class.simpleName!!
    fun reduce(params: Params)
}

data class JsonModify(
//    val table: String,
    val id: Long,
    val diff: JsonElement,
)

@Serializable
data class Task(
    val name: String,
    val done: Boolean,
)

class JsonCrudMutator(
    val store: KeyValueStore<Long, Task>
) : Mutator<JsonModify> {
    override fun reduce(params: JsonModify) {
        store[params.id] = jsonMerge(Task.serializer(), store[params.id], params.diff)
    }
}

class Tasks(
    val store: KeyValueStore<Long, Task>
) {
    val queue = OperationsQueue()

    val jsonCrud = JsonCrudMutator()

    fun set(id: Long, component: Task) {
        queue.insert(jsonCrud, JsonModify(id, Json.encodeToJsonElement(Task.serializer(), component)))
    }

    fun update(id: Long, modify: Task.() -> Task) {
        val existing = store[id] ?: return
        queue.insert(jsonCrud, JsonModify(id, jsonSubtract(Task.serializer(), existing.modify(), existing)))
    }

    fun get(id: Long) = store[id]
}

fun main() {
    Tasks.set(1, Task("Hello", false))
    Tasks.update(1) { copy(done = true) }
    Tasks.get(1)?.let { println(it) }
}

enum class OperationType {
    PUT, REMOVE
}

@Serializable
data class Operation<K, V>(val key: K, val value: V?)

interface KeyValueStore<K, V> {
    operator fun get(key: K): V?
    operator fun set(key: K, value: V)
    fun remove(key: K)

    fun apply(operation: Operation<K, V>) {
        if (operation.value == null) remove(operation.key)
        else set(operation.key, operation.value)
    }
}

interface TimeKeyValueStore<K, V> : KeyValueStore<K, V> {
    fun getModifyTime(key: K): Long?
    fun getOperationsSince(time: Long): List<Operation<K, V>>
}

@Serializable
data class EncodedMutator(
    val name: String,
    val data: ByteArray,
)

enum class MutatorResponse {
    OK
}

@Rpc
interface SyncService<K, V>: RemoteService {
    fun sendMutators(mutators: Flow<EncodedMutator>): Flow<MutatorResponse>

    fun receiveRowUpdates(): Flow<Operation<K, V>>
}

interface MutatorDecoder {
    fun reduce(encodedMutator: EncodedMutator)
}
class SyncServiceImpl<K ,V>(
    val store: TimeKeyValueStore<K, V>,
    val mutatorDecoder: MutatorDecoder,
    override val coroutineContext: CoroutineContext
): SyncService<K, V> {
    override fun sendMutators(mutators: Flow<EncodedMutator>): Flow<MutatorResponse> {
        return mutators.map {
            mutatorDecoder.reduce(it)
            MutatorResponse.OK
        }
    }

    override fun receiveRowUpdates(): Flow<Operation<K, V>> {
        TODO("Not yet implemented")
    }

}

suspend fun client(
    queue: OperationsQueue,
    store: KeyValueStore<Long, Task>,
    sync: SyncService<Long, Task>
) = coroutineScope{
    launch {
        sync.receiveRowUpdates().collect {
            store.apply(it)
        }
    }
    launch {
        sync.sendMutators(flow { queue.operations.forEach { emit(it) } }).collect {
            queue.operations.poll()
        }
    }
}
