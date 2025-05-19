package me.dvyy.syncengine.jsondiff

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals


class JsonDiffTest {
    @Serializable
    data class Task(val name: String, val done: Boolean)

    @Test
    fun `should add diff correctly`() {
        val diffed = jsonMerge(Task.serializer(), Task("One", false), Json.decodeFromString("""{ "done": true }"""))
        assertEquals(diffed, Task("One", true))
    }

    @Test
    fun `should remove diff correctly`() {
        assertEquals(
            jsonSubtract(Task.serializer(), Task("World", false), Task("Hello", false)),
            Json.decodeFromString("""{ "name": "World" }""")
        )
    }
}
