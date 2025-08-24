package me.dvyy.syncengine.client.mutators

import androidx.sqlite.SQLiteStatement
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import me.dvyy.sqlite.Database
import me.dvyy.sqlite.Transaction
import me.dvyy.sqlite.WriteTransaction
import me.dvyy.syncengine.schema.AbstractMutator
import me.dvyy.syncengine.schema.Mutators

class MutatorQueue<T, M : AbstractMutator<T>>(
    val db: Database,
    val dao: T,
    val mutatorSerializer: KSerializer<M>,
) : Mutators<M> {
    //    val protobuf = ProtoBuf {
//        serializersModule = SerializersModule {
////            contextual(JsonElement::class, JsonElementAsStringSerializer)
////            contextual(Uuid::class, UuidSerializer)
//        }
//    }
    private var previous: AbstractMutator<T>? = null

    override suspend fun invoke(mutator: M) = db.write {
        mutator.mutate(dao) //TODO merge with last if possible

        append(mutator)
    }

    fun SQLiteStatement.getMutator(index: Int) = Json.decodeFromString(mutatorSerializer, getText(index))

    context(tx: Transaction)
    inline fun forEachMutator(run: (M) -> Unit) {
        tx.forEach("SELECT json(data) FROM mutators") {
//            run(protobuf.decodeFromByteArray(mutatorSerializer, getBlob(0)))
            run(getMutator(0))
        }
    }

    context(tx: WriteTransaction)
    fun invokeAllStored() = forEachMutator { it.mutate(dao) }

    context(tx: WriteTransaction)
    fun append(mutator: M) {
        val reduced = previous?.let { mutator.reduce(it) }
        if (reduced != null) {
            // replace last mutator with reduced
            tx.exec(
                "UPDATE mutators SET data = jsonb(?) WHERE id = (SELECT max(id) FROM mutators)",
                Json.encodeToString(mutatorSerializer, mutator),
            )
            previous = reduced
        } else {
            // add as new mutator
            tx.exec("INSERT INTO mutators(data) VALUES (jsonb(?))", Json.encodeToString(mutatorSerializer, mutator))
            previous = mutator
        }
//        tx.exec("INSERT INTO mutators(data) VALUES (?)", protobuf.encodeToByteArray(mutatorSerializer, mutator))
    }

    context(tx: Transaction)
    fun getAllEncoded() = tx.getList("SELECT data FROM mutators") { getBlob(0) }

    context(tx: Transaction)
    fun firstMutatorId() = tx.getOrNull("SELECT min(id) FROM mutators") { getLong(0) }

    context(tx: WriteTransaction)
    fun clearAcknowledged(lastAcknowledged: Long) {
        tx.exec("DELETE FROM mutators WHERE id <= ?", lastAcknowledged)
    }
}
