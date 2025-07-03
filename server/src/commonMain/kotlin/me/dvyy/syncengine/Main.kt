package me.dvyy.syncengine

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import me.dvyy.syncengine.db.Database
import me.dvyy.syncengine.schema.Schema
import me.dvyy.syncengine.schema.asServerSchema
import me.dvyy.syncengine.schema.jsonTable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
suspend fun main() {
    val schema = Schema(
        listOf(jsonTable("test")),
        listOf()
    ).asServerSchema()
    val db = Database(
        driver = BundledSQLiteDriver(),
        path = "/var/home/offz/projects/syncengine/server.db"
    )
    db.write {
        schema.initialize()
        val uuid = Uuid.random()
        exec(
            "INSERT INTO test(id, owner, data) VALUES (?,?,?)",
            uuid,
            1,
            "{}".toByteArray(),
            0
        )
        exec("UPDATE test SET data = ? WHERE id = ?", "helo world".toByteArray(), uuid)
    }
//    SyncServerDataStore(
//    )
}