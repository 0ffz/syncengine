import kotlinx.coroutines.selects.select
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.ULongIdTable
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.ULongEntity
import org.jetbrains.exposed.v1.dao.ULongEntityClass
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import java.util.UUID

//import ComponentEntityClass.Companion.nowUTC
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.channelFlow
//import org.jetbrains.exposed.dao.*
//import org.jetbrains.exposed.dao.id.EntityID
//import org.jetbrains.exposed.dao.id.IntIdTable
//import org.jetbrains.exposed.dao.id.ULongIdTable
//import org.jetbrains.exposed.sql.Column
//import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
//import org.jetbrains.exposed.sql.exposedLogger
//import org.jetbrains.exposed.sql.insert
//import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
//import java.util.*
//import java.util.concurrent.ConcurrentHashMap
//import kotlin.time.Clock
//import kotlin.time.ExperimentalTime
//
suspend fun main() {
    val database = R2dbcDatabase.connect()
    suspendTransaction {
        (EntityTable).selectAll()
            .where { EntityTable.uuid eq UUID.randomUUID() }
            .collect { it[EntityTable.uuid] = UUID.randomUUID() }
    }
}
//suspend fun main() {
////    val jdbcUrl = "jdbc:sqlite::memory"
////    val database = Database.connect(jdbcUrl, "org.sqlite.JDBC")
//////        val connection: Connection = DriverManager.getConnection(jdbcUrl)//:/storage/emulated/0/sample.db")
//////        println("Connection: ${connection.isClosed}")
//////        val database = Database.connect({ connection })
////    val entity = newSuspendedTransaction(Dispatchers.IO, database) {
////        SchemaUtils.create(.TaskTable)
////        TaskEntity.new { name = "Test 1"; done = false }
////    }
////    entity.name = "Test 2"
////    println(entity.writeValues)
////    newSuspendedTransaction(Dispatchers.IO, database) {
////        entity.done = true
////    }
////    println(entity.writeValues)
////    newSuspendedTransaction(Dispatchers.IO, database) {
////        .TaskTable.selectAll().toList().toString()
////    }.let { println(it) }
//}
//
object EntityTable : ULongIdTable() {
    val uuid = uuid("uuid").uniqueIndex().autoGenerate()
    val created = long("created").nullable()//.clientDefault { nowUTC() }
    val modified = long("updated").nullable()
}

class EntityDAO(id: EntityID<ULong>) : ULongEntity(id) {
    var uuid by EntityTable.uuid
    var created by EntityTable.created
    var modified by EntityTable.modified
    companion object : ULongEntityClass<EntityDAO>(EntityTable) {
    }
}
//class EntityDAO(id: EntityID<ULong>) : ULongEntity(id) {
//    var uuid by EntityTable.uuid
//    var created by EntityTable.created
//    var modified by EntityTable.modified
//
//    fun <T> get(compClass: ComponentEntityClass<*, T>): T? {
//        return compClass.get(id.value)
//    }
//
//    fun <T, DAO : ComponentDAO> set(compClass: ComponentEntityClass<DAO, T>, block: DAO.() -> Unit) {
//        compClass.modify(this) {
//            block()
//        }
//    }
//
//    fun <T : RelationTable> addRelation(table: T, other: EntityDAO) {
//        table.insert {
//            it[sourceEntity] = this@EntityDAO.id
//            it[targetEntity] = other.id
//        }
//    }
//
//    fun <T : RelationTable> getRelated(table: T): List<EntityDAO> {
//        return table.select(table.targetEntity)
//            .where { table.sourceEntity eq id }
//            .map { EntityDAO[it[table.targetEntity]] }
//    }
//
//    companion object : ULongEntityClass<EntityDAO>(EntityTable) {
//        fun new() = new { }
//        fun get(uuid: UUID) = find(EntityTable.uuid eq uuid).firstOrNull()
//    }
//}
//
//
//abstract class RelationTable(name: String) : IntIdTable(name) {
//    val sourceEntity = reference("source", EntityTable)
//    val targetEntity = reference("target", EntityTable)
//    val created = long("created").clientDefault { nowUTC() }
//}
//
//abstract class ComponentTable(name: String = "") : IntIdTable(name) {
//    val entity = reference("entity", EntityTable)
//    val created = long("created").clientDefault { nowUTC() }
//
//    //        .defaultExpression(CurrentDateTime)
//    val modified = long("updated").nullable()
//}
//
//abstract class ComponentDAO(id: EntityID<Int>, table: ComponentTable) : IntEntity(id) {
//    var entity by EntityDAO.referencedOn(table.entity)
//    val created: Long by table.created
//    var modified: Long? by table.modified
//
//    override fun flush(batch: EntityBatchUpdate?): Boolean {
//        println("Flushing $writeValues")
//        return super.flush(batch)
//    }
//}
//
//abstract class ComponentEntityClass<E : ComponentDAO, Comp>(
//    val componentTable: ComponentTable,
//    val mapper: ComponentMapper<Comp, E>,
//) : IntEntityClass<E>(componentTable) {
//    abstract fun fromDAO(dao: E): Comp
//    abstract fun toDAO(component: Comp): E
//
//    private val listeners: ConcurrentHashMap<Long, (E) -> Unit> =
//        ConcurrentHashMap()
//
//    val entityCache = ConcurrentHashMap<Long, E>()
//
//    fun registerListener(entityId: Long, listener: (E) -> Unit) {
//        listeners[entityId] = listener
//    }
//
//    fun unregisterListener(entityId: Long) {
//        listeners.remove(entityId)
//    }
//
//    fun get(id: ULong): Comp? {
//        return find { componentTable.entity eq id }.firstOrNull()?.let { mapper.fromDAO(it) }
//    }
//
//    fun observe(id: ULong): Flow<Comp?> = channelFlow {
//        val task = get(id)
//        channel.send(task) //TODO fix
//        try {
//            registerListener(id.toLong()) {
//                channel.trySend(mapper.fromDAO(it))
//            }
//        } finally {
//            unregisterListener(id.toLong())
//        }
//    }
//
//    fun modify(entity: EntityDAO, block: E.() -> Unit) {
//        val id = entity.id.value.toLong()
//        val existing = entityCache.get(id)
//        val updatedEntity = existing?.also { it.block() }
//            ?: new {
//                block()
//                this.entity = entity
//            }.also { entityCache.put(id, it) }
//    }
//
//    suspend fun flush() = newSuspendedTransaction(Dispatchers.IO) {
//        this@ComponentEntityClass.entityCache.values.forEach { it.flush() }
//    }
//
//    init {
//        EntityHook.subscribe { change ->
//            val changedEntity = change.toEntity(this)
//            when (val type = change.changeType) {
//                EntityChangeType.Updated -> {
//                    val now = nowUTC()
//                    changedEntity?.let {
//                        if (it.writeValues[componentTable.modified as Column<Any?>] == null) {
//                            it.modified = now
//                        }
//                    }
//                    logChange(changedEntity, type, now)
//                }
//
//                else -> logChange(changedEntity, type)
//            }
//        }
//    }
//
//    private fun logChange(entity: E?, type: EntityChangeType, dateTime: Long? = null) {
//        entity?.let {
//            val entityClassName = this::class.java.enclosingClass.simpleName
//            exposedLogger.info(
//                "$entityClassName(${it.id}) ${type.name.lowercase()} at ${dateTime ?: nowUTC()}"
//            )
//            val entityId = it.entity.id.value
//            listeners[entityId.toLong()]?.invoke(it)
//        }
//    }
//
//    companion object {
//        @OptIn(ExperimentalTime::class)
//        fun nowUTC() = Clock.System.now().toEpochMilliseconds()
//    }
//}
//
//interface ComponentMapper<Comp, DAO : ComponentDAO> {
//    fun fromDAO(dao: DAO): Comp
//    fun toDAO(component: Comp): DAO
//}
