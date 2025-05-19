//import kotlinx.serialization.json.Json
//import kotlinx.serialization.json.JsonElement
//import kotlinx.serialization.json.JsonObject
//import org.jetbrains.exposed.dao.CompositeEntity
//import org.jetbrains.exposed.dao.CompositeEntityClass
//import org.jetbrains.exposed.dao.id.CompositeID
//import org.jetbrains.exposed.dao.id.CompositeIdTable
//import org.jetbrains.exposed.dao.id.EntityID
//import org.jetbrains.exposed.sql.insert
//import org.jetbrains.exposed.sql.json.jsonb
//import java.lang.System.currentTimeMillis
//
//enum class UpdateType {
//    PUT, DELETE
//}
//
////TODO primary key entity + componentId
///**
// * Stores history of row operations across all synced tables.
// */
//object OperationsTable : CompositeIdTable("modifications") {
//    val entity = reference("entity", EntityTable).entityId()
//    val table = integer("table")
//    val type = enumeration<UpdateType>("type")
//    val data = jsonb<JsonObject>("data", Json)
//    val updateTime = long("updated").clientDefault { currentTimeMillis() }
//
//    override val primaryKey = PrimaryKey(entity, table)
//}
//
//class OperationDAO(id: EntityID<CompositeID>) : CompositeEntity(id) {
//    companion object : CompositeEntityClass<OperationDAO>(OperationsTable)
//
//    var entity by OperationsTable.entity
//    var table by OperationsTable.table
//    var type by OperationsTable.type
//    var timestamp by OperationsTable.updateTime
//    var data by OperationsTable.data
//
//    fun insertInto(table: ComponentTable) {
//        table.insert {
//            data.forEach { (key, value) ->
//                table.columns.get(1)
////                it[] = value
//            }
//            it[entity] = table.entity
//        }
//    }
//}
