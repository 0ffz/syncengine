package me.dvyy.syncengine.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import me.dvyy.syncengine.common.mutators.Increment
import me.dvyy.syncengine.common.mutators.Mutator
import java.util.concurrent.ConcurrentLinkedQueue

typealias EncodedMutator = ByteArray

@OptIn(ExperimentalSerializationApi::class)
class ClientDataStore(
    val store: ReversibleDataStore,
    val scope: CoroutineScope,
) {
    //    val encodedMutators = ConcurrentLinkedQueue<EncodedMutator>()
    val mutatorsCalled = ConcurrentLinkedQueue<Mutator>()
    var lastSyncTimestamp = 0L

    fun incrementCounter() = scope.launch {
        Increment(1, 1).invoke()
    }

    fun observe(key: Long): Flow<String?> = store.changes.receiveAsFlow().filter { it.first == key }.map { it.second }

    suspend inline operator fun <reified T : Mutator> T.invoke() {
//        val reduced = (mutatorsCalled.lastOrNull() as? T)?.let {
//            this.reduce(it)
//        }
//        if (reduced != null) {
//            encodedMutators.add()
//            encodedMutators[encodedMutators.lastIndex] = ProtoBuf.encodeToByteArray(Mutator.serializer(), reduced)
//            mutatorsCalled.[encodedMutators.lastIndex] = reduced
//        } else {
//            encodedMutators += ProtoBuf.encodeToByteArray(Mutator.serializer(), this)
        mutatorsCalled += this
//        }
        mutate(store)
    }

    //    suspend fun reconcileDiff(result: SyncResult) {
//
//    }
    suspend fun reconcileDiff(count: Int, getUpdates: () -> SyncResult.Updates) {
        store.revert()
        var last: SyncResult.Updates? = null
        repeat(count) {
            last = getUpdates()
            last.updates.forEach { (row, value) ->
                store.setUnderlying(row, value)
            }
        }
        last?.let { lastSyncTimestamp = it.lastTimestamp }
        mutatorsCalled.forEach { it.mutate(store) }
    }

    suspend fun reconcileDiff(updates: SyncResult.Updates) {
        store.revert()
        updates.updates.forEach { (row, value) ->
            store.setUnderlying(row, value)
        }
        lastSyncTimestamp = updates.lastTimestamp
        mutatorsCalled.forEach { it.mutate(store) }
    }

    companion object {
        fun decode(encodedMutator: EncodedMutator): Mutator {
            return ProtoBuf.decodeFromByteArray(Mutator.serializer(), encodedMutator)
        }
    }
}


