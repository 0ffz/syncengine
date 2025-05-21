package me.dvyy.syncengine.common

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import me.dvyy.syncengine.common.mutators.Increment
import me.dvyy.syncengine.common.mutators.Mutator
import java.util.concurrent.ConcurrentLinkedQueue

typealias EncodedMutator = ByteArray

@OptIn(ExperimentalSerializationApi::class)
class ClientDataStore(
    val store: ReversibleDataStore,
) {
    val encodedMutators = ConcurrentLinkedQueue<EncodedMutator>()
    val mutatorsCalled = ConcurrentLinkedQueue<Mutator>()
    var lastSyncTimestamp = 0L

    suspend fun test() {
        Increment(1, 1).invoke()
    }

    suspend inline operator fun <reified T : Mutator> T.invoke() {
//        val reduced = (mutatorsCalled.lastOrNull() as? T)?.let {
//            this.reduce(it)
//        }
//        if (reduced != null) {
//            encodedMutators.add()
//            encodedMutators[encodedMutators.lastIndex] = ProtoBuf.encodeToByteArray(Mutator.serializer(), reduced)
//            mutatorsCalled.[encodedMutators.lastIndex] = reduced
//        } else {
            encodedMutators += ProtoBuf.encodeToByteArray(Mutator.serializer(), this)
            mutatorsCalled += this
//        }
        mutate(store)
    }

    suspend fun reconcileDiff(result: SyncResult) {
        store.revert()
        result.diffs.forEach { (row, value) ->
            store.setUnderlying(row, value)
        }
        lastSyncTimestamp = result.lastModificationTimestamp
        repeat(result.lastMutatorApplied) {
            mutatorsCalled.remove()
        }
        mutatorsCalled.forEach { it.mutate(store) }
    }

    companion object {
        fun decode(encodedMutator: EncodedMutator): Mutator {
            return ProtoBuf.decodeFromByteArray(Mutator.serializer(), encodedMutator)
        }
    }
}


