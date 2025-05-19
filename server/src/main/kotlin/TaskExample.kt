//import me.dvyy.syncengine.shared.SubtaskRelation
//import me.dvyy.syncengine.shared.TaskDAO
//import me.dvyy.syncengine.shared.TaskTable
//import org.jetbrains.exposed.sql.Database
//import org.jetbrains.exposed.sql.SchemaUtils
//import org.jetbrains.exposed.sql.transactions.transaction
//
//fun main() {
//    val jdbcUrl = "jdbc:sqlite:test.db"
//    val database = Database.connect(jdbcUrl, "org.sqlite.JDBC")
//    transaction {
//        SchemaUtils.create(EntityTable, TaskTable, SubtaskRelation)
//    }
//    val entity1 = transaction {
//        val entity1 = EntityDAO.new()
//        val entity2 = EntityDAO.new()
//        entity1.addRelation(SubtaskRelation, entity2)
//        entity1
//    }
//    entity1.set(TaskDAO) {
//        name = ".Task"
//        done = true
//    }
//    entity2.set(TaskDAO) {
//        name = "Subtask"
//        done = false
//    }
//    val subtasks = transaction { entity1.getRelated(SubtaskRelation).map { it.get(TaskDAO) } }
//    println(subtasks)
//}
