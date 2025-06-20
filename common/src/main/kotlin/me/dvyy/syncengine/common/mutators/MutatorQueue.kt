package me.dvyy.syncengine.common.mutators

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.dvyy.syncengine.common.tables.MutatorsTable
import me.dvyy.syncengine.common.tables.launchTransaction
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.inSubQuery
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import java.util.concurrent.ConcurrentLinkedQueue

class MutatorQueue {
    val inMemoryQueue = ConcurrentLinkedQueue<Mutator>()
    val mutatorScope = CoroutineScope(Dispatchers.IO.limitedParallelism(1))
    var applying = false

    private suspend fun scheduleApply() {
        if (applying) return
        applying = true
        launchTransaction {
            applyQueued()
        }.await()
        applying = false
    }

    fun callMutator(mutator: Mutator) {
        mutatorScope.launch {
            inMemoryQueue.add(mutator) //TODO this could lead to a mutator not being applied since we are currently applying
            scheduleApply()
        }
    }

    fun applyQueued() {
        repeat(inMemoryQueue.size) {
            val mutator = inMemoryQueue.poll()
            mutator.mutate()
            MutatorsTable.insert { it[data] = mutator }
        }
    }

    fun reconcileStored() {
        MutatorsTable.selectAll().forEach {
            it[MutatorsTable.data].mutate()
        }
        applyQueued()
    }

    fun clearMutators(count: Int) {
        val sqlCount = MutatorsTable.deleteWhere {
            id inSubQuery select(id).limit(count)
        }
        val remaining = sqlCount - count
//        repeat(remaining) { inMemoryQueue.poll() }
    }

    fun getMutatorsToSend(): List<Mutator> {
        applyQueued()
        return MutatorsTable.selectAll().map { it[MutatorsTable.data] }
    }
}
