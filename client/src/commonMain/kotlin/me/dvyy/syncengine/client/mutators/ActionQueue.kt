package me.dvyy.syncengine.client.mutators

import androidx.sqlite.SQLiteStatement
import co.touchlab.kermit.Logger
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import me.dvyy.sqlite.Database
import me.dvyy.sqlite.Transaction
import me.dvyy.sqlite.WriteTransaction
import me.dvyy.syncengine.actions.Action
import me.dvyy.syncengine.actions.Actions
import me.dvyy.syncengine.client.schema.ClientQueries
import me.dvyy.syncengine.reducers.Reducers

/**
 * Mutators are operations ran in-order to modify a client's state.
 *
 * These are stored locally in a table via this MutatorQueue until a sync occurs,
 * when they are read and sent off to the server to apply, being cleared once the server confirms a sync request
 */
class ActionQueue(
    private val logger: Logger,
    private val db: Database,
    private val reducers: Reducers,
) : Actions {
    private val queries = ClientQueries()
    private val json = Json {
        serializersModule = reducers.serializersModule
    }
    private val actionSerializer = PolymorphicSerializer(Action::class)
    private var previous: Action? = null

    override suspend fun invoke(action: Action) = db.write {
        applyReducersFor(action) //TODO merge with last if possible
        append(action)
    }

    context(tx: WriteTransaction)
    private fun applyReducersFor(action: Action) {
        logger.v { "Applying action: $action" }
        reducers.actions[action::class]?.invoke(tx, action)
    }

    fun SQLiteStatement.getMutator(index: Int) = json.decodeFromString(actionSerializer, getText(index))

    context(tx: Transaction)
    inline fun forEachMutator(run: (Action) -> Unit) {
        tx.forEach("SELECT json(data) FROM actions_list") {
//            run(protobuf.decodeFromByteArray(mutatorSerializer, getBlob(0)))
            run(getMutator(0))
        }
    }

    context(tx: WriteTransaction)
    fun invokeAllStored() = forEachMutator { applyReducersFor(it) }

    context(tx: WriteTransaction)
    fun append(action: Action) {
        val reduced = previous?.let { action.reduce(it) }
        if (reduced != null) {
            logger.v { "Appending reduced action: $reduced" }
//            // replace last action with reduced
            queries.actions.updateLastAction(json.encodeToString(actionSerializer, action))
            previous = reduced
        } else {
            logger.v { "Appending new action: $action" }
            // add as new action
            queries.actions.append(json.encodeToString(actionSerializer, action))
            previous = action
        }
    }

    context(tx: WriteTransaction)
    fun preventPreviousReducing() {
        previous = null
    }

    context(tx: Transaction)
    fun getAllEncoded(): List<ByteArray> = queries.actions.getAll().map { getBlob(0) }

    context(tx: Transaction)
    fun firstMutatorId(): Long = queries.actions.firstActionId().firstOrNull { getLong(0) } ?: -1

    context(tx: WriteTransaction)
    fun clearAcknowledged(lastAcknowledged: Long) {
        queries.actions.clearAcknowledged(lastAcknowledged)
    }
}
