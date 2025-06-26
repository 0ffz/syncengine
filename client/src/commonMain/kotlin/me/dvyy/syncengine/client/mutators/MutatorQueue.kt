package me.dvyy.syncengine.client.mutators

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import me.dvyy.syncengine.db.Database
import me.dvyy.syncengine.db.Transaction
import me.dvyy.syncengine.db.WriteTransaction
import me.dvyy.syncengine.schema.AbstractMutator
import me.dvyy.syncengine.schema.Mutators

class MutatorQueue<T, M : AbstractMutator<T>>(
    val db: T,
    val mutatorSerializer: KSerializer<M>,
) : Mutators<M> {
    override suspend fun invoke(mutator: M) = Database.write {
        mutator.mutate(db) //TODO merge with last if possible
        append(mutator)
    }

    context(tx: Transaction)
    inline fun forEachMutator(run: (M) -> Unit) {
        tx.forEach("SELECT json(data) FROM mutators") {
            run(Json.decodeFromString(mutatorSerializer, getText(0)))
        }
    }

    context(tx: WriteTransaction)
    fun append(mutator: M) {
        tx.exec("INSERT INTO mutators(data) VALUES (jsonb(?))", Json.encodeToString(mutatorSerializer, mutator))
    }

    context(tx: Transaction)
    fun getAllEncoded() = tx.getList("SELECT data FROM mutators") { getBlob(0) }

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
