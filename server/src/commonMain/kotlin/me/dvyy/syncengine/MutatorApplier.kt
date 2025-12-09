package me.dvyy.syncengine

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import me.dvyy.sqlite.Transaction
import me.dvyy.sqlite.WriteTransaction
import me.dvyy.syncengine.schema.AbstractMutator
import me.dvyy.syncengine.server.schema.ServerDatabase
import me.dvyy.syncengine.sync.SyncRequest
import kotlin.uuid.ExperimentalUuidApi

/**
 * Applies mutators from clients to the server's database,
 * handling cases where a client re-submits mutators that were already applied
 */
class MutatorApplier<T, M : AbstractMutator<T>>(
    val dao: T,
    val mutatorSerializer: KSerializer<M>,
) {
    private val server = ServerDatabase()

    @OptIn(ExperimentalUuidApi::class)
    context(tx: WriteTransaction)
    fun applyMutations(request: SyncRequest) {
        val mutators = request.encodedMutators

        // Last mutator we've applied on the server
        val lastApplied = server.clients.getLastMutatorApplied(request.deviceId, tx.identity)
            .firstOrNull { getLong(it.last_mutator_applied) }
            ?: -1
        // Last mutator applied after this transaction completes (will update previous value)
        val lastAppliedAfterTransaction = mutators.lastIndex + request.firstMutatorId

        // Skip mutators we've already applied, ex. after a retry from a failed response over network
        // When all is well
        val startIndex = (lastApplied - request.firstMutatorId - 1)
            .coerceAtLeast(0)
            .toInt()
            // If the amount to skip is greater than the number of mutators in the request
            // we can assume a wrap-around happened, or the client
            .let { if (it > mutators.lastIndex) 0 else it }
        for (index in startIndex..mutators.lastIndex) {
            val mutator = decodeMutator(mutators[index])
            mutator.mutate(dao)
        }

        //TODO ensure clienttable populated before this

        // Write last mutator applied for this client
        server.clients.setLastMutatorApplied(lastAppliedAfterTransaction, uuid = request.deviceId, owner = tx.identity)
    }

    context(tx: Transaction)
    fun decodeMutator(byteArray: ByteArray): M {
        return tx
            .select("SELECT json(?)", byteArray)
            .first { Json.decodeFromString(mutatorSerializer, getText(0)) }
    }
}
