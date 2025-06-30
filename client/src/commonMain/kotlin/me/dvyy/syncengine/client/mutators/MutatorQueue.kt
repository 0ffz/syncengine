package me.dvyy.syncengine.client.mutators

import androidx.sqlite.SQLiteStatement
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import me.dvyy.syncengine.db.Database
import me.dvyy.syncengine.db.Transaction
import me.dvyy.syncengine.db.WriteTransaction
import me.dvyy.syncengine.schema.AbstractMutator
import me.dvyy.syncengine.schema.Mutators
import kotlin.uuid.ExperimentalUuidApi

class MutatorQueue<T, M : AbstractMutator<T>>(
    val db: T,
    val mutatorSerializer: KSerializer<M>,
) : Mutators<M> {
    @OptIn(ExperimentalUuidApi::class)
//    val protobuf = ProtoBuf {
//        serializersModule = SerializersModule {
////            contextual(JsonElement::class, JsonElementAsStringSerializer)
////            contextual(Uuid::class, UuidSerializer)
//        }
//    }
    override suspend fun invoke(mutator: M) = Database.write {
        mutator.mutate(db) //TODO merge with last if possible

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
    fun append(mutator: M) {
        val previous = getLast()
        val reduced = if (previous != null) mutator.reduce(previous.second) else null
        if (previous != null && reduced != null) {
            // replace last mutator with reduced
            tx.exec(
                "UPDATE mutators SET data = jsonb(?) WHERE id = ?",
                Json.encodeToString(mutatorSerializer, mutator),
                previous.first
            )
        } else {
            // add as new mutator
            tx.exec("INSERT INTO mutators(data) VALUES (jsonb(?))", Json.encodeToString(mutatorSerializer, mutator))
        }
//        tx.exec("INSERT INTO mutators(data) VALUES (?)", protobuf.encodeToByteArray(mutatorSerializer, mutator))
    }

    context(tx: WriteTransaction)
    fun getLast(): Pair<Int, M>? {
        return tx.getOrNull("SELECT id, json(data) FROM mutators ORDER BY id DESC LIMIT 1") {
            getInt(0) to getMutator(1)
        }
    }


    context(tx: Transaction)
    fun getAllEncoded() = tx.getList("SELECT data FROM mutators") { getBlob(0) }
}
