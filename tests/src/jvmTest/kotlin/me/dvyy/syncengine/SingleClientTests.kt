package me.dvyy.syncengine

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import me.dvyy.sqlite.Database
import me.dvyy.syncengine.client.sync.SyncClient
import me.dvyy.syncengine.jsonactions.JsonDataQueries
import me.dvyy.syncengine.jsonactions.actions.DeleteRowAction
import me.dvyy.syncengine.jsonactions.actions.JsonCreateAction
import me.dvyy.syncengine.jsonactions.actions.JsonPatchAction
import me.dvyy.syncengine.jsonactions.reducers.jsonReducers
import me.dvyy.syncengine.reducers.reducers
import me.dvyy.syncengine.reducers.syncProtocol
import me.dvyy.syncengine.schema.jsonTable
import me.dvyy.syncengine.schema.schema
import me.dvyy.syncengine.server.schema.SyncServer
import me.dvyy.syncengine.server.schema.mockAwaitingService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class SingleClientTests {
    val clientDatabase = Database.temporary()
    val serverDatabase = Database.temporary()
    val tasksTable = jsonTable("tasks")
    val schema = schema(shared = setOf(tasksTable), protocol = syncProtocol {
        action<JsonCreateAction>(1)
        action<JsonPatchAction>(2)
        action<DeleteRowAction>(3)
    })
    val tasks = JsonDataQueries(Task.serializer(), tasksTable)
    val reducers = reducers {
        jsonReducers(listOf(tasks))
    }
    val server = SyncServer.of(serverDatabase, reducers, schema)
    val mockService = server.mockAwaitingService(user = 0)
    val client = SyncClient.of(clientDatabase, reducers, schema, mockService)

    @Test
    fun `should sync multiple actions correctly`() = runTest {
        // arrange
        client.initialize()
        server.initialize()

        val json = Json.decodeFromString<JsonElement>("""{ "text":  "hello world" }""")
        val json2 = Json.decodeFromString<JsonElement>("""{ "text":  "hello world 2" }""")

        val id = Uuid.random()
        val id2 = Uuid.random()

        // act
        client.invoke(JsonCreateAction(table = "tasks", id = id, data = json))
        client.invoke(JsonCreateAction(table = "tasks", id = id2, data = json))
        client.invoke(JsonPatchAction(table = "tasks", id = id, patch = json2)) // will get reduced with previous
        launch { client.sync() }
        mockService.sendRequest()
        mockService.respond()
        val serverTask = serverDatabase.read { tasks.get(id) to tasks.get(id2) }
        val clientTask = clientDatabase.read { tasks.get(id) to tasks.get(id2) }

        // assert
        val expected = Task(text = "hello world 2") to Task(text = "hello world")
        assertEquals(expected, serverTask)
        assertEquals(expected, clientTask)
    }

    @Test
    fun `should correctly reconcile reduceable local changes which occurred after sync`() = runTest {
        // arrange
        client.initialize()
        server.initialize()

        val task = Json.decodeFromString<JsonElement>("""{ "text":  "original" }""")
        val taskPatch1 = Json.decodeFromString<JsonElement>("""{ "text":  "patched1" }""")
        val taskPatch2 = Json.decodeFromString<JsonElement>("""{ "text":  "patched2" }""")
        val id = Uuid.random()

        // act
        client.invoke(JsonCreateAction(table = "tasks", id = id, data = task))
        client.invoke(JsonPatchAction(table = "tasks", id = id, patch = taskPatch1))
        launch { client.sync() }
        mockService.sendRequest() // request arrives at server, which processs it
        client.invoke(JsonPatchAction(table = "tasks", id = id, patch = taskPatch2))
        mockService.respond() // server responds without knowing about change above
        val serverTask = serverDatabase.read { tasks.get(id) }
        val clientTask = clientDatabase.read { tasks.get(id) }

        // assert
        assertEquals(Task(text = "patched1"), serverTask)
        assertEquals(Task(text = "patched2"), clientTask)
    }
}