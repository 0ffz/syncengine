package me.dvyy.syncengine.schema

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import me.dvyy.syncengine.db.Database
import me.dvyy.syncengine.db.Transaction
import me.dvyy.syncengine.db.WriteTransaction

interface Mutators<T, M: AbstractMutator<T>> {
    suspend operator fun invoke(mutator: M)
}

interface AbstractMutator<T> {
    context(tx: WriteTransaction)
    fun mutate(db: T)

    fun reduce(previous: AbstractMutator<T>): AbstractMutator<T>? = null
}

class MutatorQueue<T, M: AbstractMutator<T>>(
    val db: T,
    val mutatorSerializer: KSerializer<M>
): Mutators<T, M> {
//    val inMemoryQueue = ConcurrentLinkedQueue<Mutator>()
    val mutatorScope = CoroutineScope(Dispatchers.IO.limitedParallelism(1))
    var applying = false

//    private suspend fun scheduleApply() {
//        if (applying) return
//        applying = true
//        Database.write {
//            applyQueued()
//        }
//        applying = false
//    }
//    mutatorScope.launch {
//        inMemoryQueue.add(mutator) //TODO this could lead to a mutator not being applied since we are currently applying
//        scheduleApply()
//    }

    override suspend fun invoke(mutator: M) = Database.write {
        mutator.mutate(db) //TODO merge with last if possible
        append(mutator)
    }

    context(tx: Transaction)
    fun getAllEncoded() = tx.getList("SELECT data FROM mutators") { getBlob(0) }

    context(tx: Transaction)
    inline fun forEachMutator(run: (M) -> Unit) {
        tx.forEach("SELECT json(data) FROM mutators") {
            run(Json.decodeFromString(mutatorSerializer, getText(0)))
        }
    }

    context(tx: WriteTransaction)
    fun append(mutator: M) {
        tx.exec("INSERT INTO mutators(data) VALUES (?)", ProtoBuf.encodeToByteArray(mutatorSerializer, mutator))
    }
//    fun applyQueued() {
//        repeat(inMemoryQueue.size) {
//            val mutator = inMemoryQueue.poll()
//            mutator.mutate()
//            MutatorsTable.insert { it[data] = mutator }
//        }
//    }

//    suspend fun reconcileStored() = Database.write {
//        MutatorsTable.forEachMutator { it.mutate() }
//        applyQueued()
//    }
//
//    fun clearMutators(count: Int) {
//        val sqlCount = MutatorsTable.deleteWhere {
//            id inSubQuery select(id).limit(count)
//        }
//        val remaining = sqlCount - count
////        repeat(remaining) { inMemoryQueue.poll() }
//    }
//
//    fun getMutatorsToSend(): List<Mutator> {
//        applyQueued()
//        return MutatorsTable.selectAll().map { it[MutatorsTable.data] }
//    }
}
