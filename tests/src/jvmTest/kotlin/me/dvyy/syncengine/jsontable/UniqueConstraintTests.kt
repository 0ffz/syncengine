package me.dvyy.syncengine.jsontable

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowAny
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import me.dvyy.sqlite.Database
import me.dvyy.syncengine.client.mutators.RollbackJsonTable
import me.dvyy.syncengine.jsonactions.JsonDataQueries
import me.dvyy.syncengine.schema.jsonTable
import kotlin.test.Test
import kotlin.uuid.Uuid

class UniqueConstraintTests {
    val table = RollbackJsonTable(jsonTable("test") {
        index("parent", "data ->> '$.parent'", unique = true)
    })

    @Test
    fun `should error when upserting and unique constraint fails`() = runTest {
        // arrange
        val db = Database.temporary()
        val query = JsonDataQueries(String.serializer(), table)
        val a = Uuid.random()
        val b = Uuid.random()

        // act
        // create two items
        db.write {
            table.create()
            query.create(a, buildJsonObject { put("parent", "a") })
            query.create(b, buildJsonObject { put("parent", "b") })
        }

        // assert
        shouldThrowAny {
            db.write {
                query.upsert(a, buildJsonObject { put("parent", "b") })
            }
        }
    }

    @Test
    fun `should be able to rollback when violating a UNIQUE constraint`() = runTest {
        // arrange
        val db = Database.temporary()
        val query = JsonDataQueries(String.serializer(), table)
        val a = Uuid.random()
        val b = Uuid.random()

        // act
        // create two items
        db.write {
            table.create()
            query.create(a, buildJsonObject { put("parent", "a") })
            query.create(b, buildJsonObject { put("parent", "b") })
            exec("UPDATE test SET original_data = data")
        }

        // Update such that last item references original parent of first
        db.write {
            query.upsert(a, buildJsonObject { put("parent", "c") })
            query.upsert(b, buildJsonObject { put("parent", "a") })
        }

        // assert
        shouldNotThrowAny {
            db.write {
                table.rollback()
            }
        }
    }
}