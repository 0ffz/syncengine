package me.dvyy.syncengine.shared

import kotlinx.serialization.Serializable

//object TaskTable : ComponentTable("tasks") {
//    val name = varchar("name", 255)
//    val done = bool("done")
//}
//
//class TaskDAO(id: EntityID<Int>) : ComponentDAO(id, TaskTable) {
//    companion object : ComponentEntityClass<TaskDAO, Task>(TaskTable) {
//        override fun fromDAO(dao: TaskDAO): Task {
//            TODO("Not yet implemented")
//        }
//
//        override fun toDAO(component: Task): TaskDAO {
//            TODO("Not yet implemented")
//        }
//    }
//        Task(it.name, it.done)
//
//    var name by TaskTable.name
//    var done by TaskTable.done
//}
//
//object SubtaskRelation : RelationTable("subtask")
