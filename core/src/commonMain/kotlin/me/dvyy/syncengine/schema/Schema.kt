package me.dvyy.syncengine.schema

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.protobuf.ProtoBuf
import me.dvyy.syncengine.db.Database
import me.dvyy.syncengine.db.WriteTransaction

class Schema(
    val syncedTables: List<RollbackTable>,
) {
    suspend fun initTables() {
        Database.Companion.write {
            syncedTables.forEach { it.create() }
        }
    }

    context(tx: WriteTransaction)
    fun rollbackAll() {
        syncedTables.forEach { it.rollback() }
    }
}
@Serializable
data class Test(
    val someLongName: String = "",
    val someInt: Int? = null,
)

fun main() {
    println(ProtoBuf.encodeToByteArray(Test.serializer(), Test("hello world", 11235424)).toHexString())
    println(ProtoBuf.encodeToByteArray(Test.serializer(), Test("", null)).toHexString())
}