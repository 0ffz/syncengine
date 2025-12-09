package me.dvyy.syncengine.server.schema

import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import me.dvyy.sqlite.Transaction
import me.dvyy.sqlite.WriteTransaction
import me.dvyy.syncengine.actions.Action
import me.dvyy.syncengine.reducers.Reducers
import me.dvyy.syncengine.sync.SyncRequest
import kotlin.uuid.ExperimentalUuidApi

/**
 * Invokes actions from clients to the server's database,
 * handling cases where a client re-submits actions that were already applied
 */
class ServerActionProcessor(
    private val reducers: Reducers,
) {
    private val actionSerializer = PolymorphicSerializer(Action::class)
    private val json = Json { serializersModule = reducers.serializersModule }
    private val server = ServerQueries()

    @OptIn(ExperimentalUuidApi::class)
    context(tx: WriteTransaction)
    fun invokeActions(request: SyncRequest) {
        val actions = request.encodedActions

        // Last action we've applied on the server
        val lastApplied = server.clients.getLastActionApplied(request.deviceId, tx.identity)
            .firstOrNull { getLong(it.last_action_applied) }
            ?: -1
        // Last action applied after this transaction completes (will update previous value)
        val lastAppliedAfterTransaction = actions.lastIndex + request.firstActionId

        // Skip actions we've already applied, ex. after a retry from a failed response over network
        // When all is well
        val startIndex = (lastApplied - request.firstActionId - 1)
            .coerceAtLeast(0)
            .toInt()
            // If the amount to skip is greater than the number of actions in the request
            // we can assume a wrap-around happened, or the client
            .let { if (it > actions.lastIndex) 0 else it }
        for (index in startIndex..actions.lastIndex) {
            val action = decodeAction(actions[index])
            reducers.actions[action::class]?.invoke(tx, action)
        }

        //TODO ensure clienttable populated before this

        // Write last action applied for this client
        server.clients.setLastActionApplied(lastAppliedAfterTransaction, uuid = request.deviceId, owner = tx.identity)
    }

    context(tx: Transaction)
    fun decodeAction(byteArray: ByteArray): Action {
        return tx
            .select("SELECT json(?)", byteArray)
            .first { json.decodeFromString(actionSerializer, getText(0)) }
    }
}
