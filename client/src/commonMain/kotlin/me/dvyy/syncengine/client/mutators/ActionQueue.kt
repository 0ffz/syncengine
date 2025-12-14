package me.dvyy.syncengine.client.mutators

import androidx.sqlite.SQLiteStatement
import co.touchlab.kermit.Logger
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import me.dvyy.sqlite.Database
import me.dvyy.sqlite.Transaction
import me.dvyy.sqlite.WriteTransaction
import me.dvyy.syncengine.actions.Action
import me.dvyy.syncengine.actions.Actions
import me.dvyy.syncengine.actions.PolymorphicIntSerializer
import me.dvyy.syncengine.client.schema.ClientQueries
import me.dvyy.syncengine.reducers.Reducers
import me.dvyy.syncengine.schema.Schema

/**
 * Mutators are operations ran in-order to modify a client's state.
 *
 * These are stored locally in a table via this MutatorQueue until a sync occurs,
 * when they are read and sent off to the server to apply, being cleared once the server confirms a sync request
 */
@OptIn(ExperimentalSerializationApi::class)
class ActionQueue(
    private val logger: Logger,
    private val db: Database,
    private val schema: Schema,
    private val reducers: Reducers,
) : Actions {
    private val queries = ClientQueries()
    private val protobuf = ProtoBuf { }
    private val actionSerializer = PolymorphicIntSerializer.of(schema)
    private var previous: Action? = null

    override suspend fun invoke(action: Action) {
        db.write {
            applyReducersFor(action) //TODO merge with last if possible
            append(action)
        }
    }

    context(tx: WriteTransaction)
    private fun applyReducersFor(action: Action) {
        logger.v { "Applying action: $action" }
        reducers.actionsToReducers[action::class]?.invoke(tx, action)
    }

    fun SQLiteStatement.getMutator(index: Int) = protobuf.decodeFromByteArray(actionSerializer, getBlob(index))

    context(tx: Transaction)
    inline fun forEachMutator(run: (Action) -> Unit) {
        tx.forEach("SELECT data FROM actions_list") {
//            run(protobuf.decodeFromByteArray(mutatorSerializer, getBlob(0)))
            run(getMutator(0))
        }
    }

    context(tx: WriteTransaction)
    fun invokeAllStored() = forEachMutator { applyReducersFor(it) }

    context(tx: WriteTransaction)
    fun append(action: Action) {
        val reduced = previous?.let { action.reduce(it) }
        logger.v { "Previous action was $previous reduced $reduced" }
        if (reduced != null) {
            logger.v { "Appending reduced action: $reduced" }
//            // replace last action with reduced
            queries.actions.updateLastAction(protobuf.encodeToByteArray(actionSerializer, action))
            previous = reduced
        } else {
            logger.v { "Appending new action: $action" }
            protobuf.encodeToByteArray(actionSerializer, action)
            // add as new action
            try {
                queries.actions.append(protobuf.encodeToByteArray(actionSerializer, action))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            previous = action
        }
    }

    context(tx: WriteTransaction)
    fun preventPreviousReducing() {
        previous = null
    }

    context(tx: Transaction)
    fun getAllEncodedAfter(afterId: Long): List<ByteArray> = queries.actions.getAllAfterId(afterId).map { getBlob(0) }

    context(tx: Transaction)
    fun firstMutatorId(): Long = queries.actions.firstActionId().firstOrNull { getLong(0) } ?: -1

    context(tx: Transaction)
    fun count(): Long = queries.actions.count().first { getLong(it.count) }

    context(tx: WriteTransaction)
    fun clearAcknowledged(lastAcknowledged: Long) {
        queries.actions.clearAcknowledged(lastAcknowledged)
    }
}
