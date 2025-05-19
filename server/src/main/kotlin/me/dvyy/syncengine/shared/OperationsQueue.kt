package me.dvyy.syncengine.shared

import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

class OperationsQueue {
    val mutators = mapOf<String, Mutator<*>>(
        JsonCrudMutator::class.simpleName!! to JsonCrudMutator
    )

    val operations = ConcurrentLinkedQueue<EncodedMutator>()

    fun <Params> insert(mutator: Mutator<Params>, parameters: Params) {
        mutator.reduce(parameters)
        operations.add(TODO())//mutator.javaClass.simpleName + parameters)
    }
}
