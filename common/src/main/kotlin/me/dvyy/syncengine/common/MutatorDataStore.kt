package me.dvyy.syncengine.common

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import me.dvyy.syncengine.common.mutators.Increment
import me.dvyy.syncengine.common.mutators.Mutator

typealias EncodedMutator = ByteArray

@OptIn(ExperimentalSerializationApi::class)
class MutatorDataStore(
    val store: ReversibleDataStore,
) {
    val encodedMutators = mutableListOf<EncodedMutator>()
    val mutatorsCalled = mutableListOf<Mutator>()

    fun test() {
        Increment(1, 1).invoke()
    }

    inline operator fun <reified T : Mutator> T.invoke() {
        val reduced = (mutatorsCalled.lastOrNull() as? T)?.let {
            this.reduce(it)
        }
        if (reduced != null) {
            encodedMutators[encodedMutators.lastIndex] = ProtoBuf.encodeToByteArray(Mutator.serializer(), reduced)
            mutatorsCalled[encodedMutators.lastIndex] = reduced
        } else {
            encodedMutators += ProtoBuf.encodeToByteArray(Mutator.serializer(), this)
            mutatorsCalled += this
        }
        mutate(store)
    }

    fun reconcileDiff(rowDiffs: Array<RowDiff>) {
        store.revert()
        rowDiffs.forEach { (row, value) ->
            store.putUnderlying(row, value)
        }
        mutatorsCalled.forEach { it.mutate(store) }
    }

    companion object {
        fun decode(encodedMutator: EncodedMutator): Mutator {
            return ProtoBuf.decodeFromByteArray(Mutator.serializer(), encodedMutator)
        }
    }
}


