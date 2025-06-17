package me.dvyy.syncengine.ui

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.dvyy.syncengine.common.mutators.Increment
import me.dvyy.syncengine.common.mutators.MutatorQueue
import me.dvyy.syncengine.common.mutators.UpdateTask
import me.dvyy.syncengine.common.observe
import me.dvyy.syncengine.common.ui.*
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.jdbc.SizedIterable
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.measureTimedValue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

// UI can safely access this via ViewModel
open class GeneratedMutators<EntityClass : org.jetbrains.exposed.v1.dao.EntityClass<UUID, Entity>, Entity : UUIDEntity, UI : Any>(
    val entityClass: EntityClass,
    val scope: CoroutineScope,
    val toUiState: (Entity) -> UI,
    val copyToEntity: Entity.(UI) -> Unit,
) {
    //    private val cachedEntities = ConcurrentHashMap<UUID, Entity>()
    private val cachedStates = ConcurrentHashMap<UUID, MutableState<UI?>>()

    @Composable
    fun observe(id: UUID): State<UI?> {

        val state = remember(id) {
            //TODO notify of updates from DB
            cachedStates.getOrPut(id) {//TODO concurrent map
                mutableStateOf<UI?>(null).also { state ->
                    scope.launch {
                        val entity = withContext(Dispatchers.IO) {
                            transaction { entityClass[id] }
                        }
//                cachedEntities[id] =
//                    entity //TODO make sure this entity isn't reused in other queries and that data is still valid
                        val uiState = toUiState(entity)
                        Snapshot.withMutableSnapshot {
                            println("Updated ${state.value} to $uiState")
                            state.value = uiState
                        }
                        observe(null) { toUiState(get(id)) }.collect {
                            Snapshot.withMutableSnapshot {
                                state.value = it
                            }
                        }
                    }
                }
            }
        }
        //TODO on dispose, unregister MutableState, check if it works with multiple accessors?
        return state
    }

    fun mutate(uuid: UUID, edit: (UI) -> UI) {
        val cached = cachedStates[uuid]?.value
        val edited = if (cached != null) edit(cached) else null
        if (edited != null) {
            cachedStates[uuid]?.value = edited
        }
        //TODO track in mutator list
        globalMutatorQueue.callMutator(UpdateTask(uuid.toKotlinUuid(), edited!! as Task))

//        launchTransaction {
//            entityClass.findByIdAndUpdate(uuid) {
//                if (edited != null) it.copyToEntity(edited)
//                else it.copyToEntity(toUiState(it))
//            }
//        }
    }

    fun new(uiState: UI) {
        globalMutatorQueue.callMutator(UpdateTask(Uuid.random(), uiState as Task))
    }

    fun <T> observe(initial: T, query: EntityClass.() -> T): StateFlow<T> = entityClass
        .observe { query() }
        .stateIn(
            scope,
            started = SharingStarted.WhileSubscribed(5_000),
            initial
        ) //TODO not necessary since already hot?

    fun SizedIterable<Entity>.toUiState(): List<UI> = map { toUiState(it) }
}

val globalMutatorQueue = MutatorQueue()

class TaskMutators(scope: CoroutineScope) : GeneratedMutators<TaskEntity.Companion, TaskEntity, Task>(
    TaskEntity, scope,
    toUiState = { Task(it.name, it.done) },
    copyToEntity = { done = it.done; name = it.name },
)

data class TaskList(
    val name: String,
    val tasks: List<UUID>,
)

class TasksViewModel : ViewModel() {
    val tasks = TaskMutators(viewModelScope)

    fun tasksForList(listId: UUID): Flow<List<UUID>> = ListEntity.observe {
        get(listId).tasks.map { it.id.value }
    }

    //TODO clean up with context receivers
    val allTasks: StateFlow<List<UUID>> = with(tasks) {
        observe(emptyList()) {
            measureTimedValue { all().orderBy(TaskTable.name to SortOrder.DESC).map { it.id.value } }
                .also { println("Tasks READ took: " + it.duration) }
                .value
        }
    } //TODO this selects all, we just want id column
    val count = allTasks.map { it.count() }
}