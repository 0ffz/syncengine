package me.dvyy.syncengine

import io.kotest.matchers.ints.shouldBeLessThan
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ContextualSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.protobuf.ProtoBuf
import me.dvyy.sqlite.Database
import me.dvyy.syncengine.actions.Action
import me.dvyy.syncengine.actions.PolymorphicIntSerializer
import me.dvyy.syncengine.schema.JsonElementAsStringSerializer
import me.dvyy.syncengine.schema.UuidSerializer
import kotlin.test.Test
import kotlin.uuid.Uuid

class PackingTests {
    @Serializable
    data class TestAction(
        val name: String,
        val bytes: ByteArray,
        @Serializable(with = UuidSerializer::class) val uuid: Uuid,
        val jsonElement: @Serializable(with = ContextualSerializer::class) JsonElement,
    ) : Action

    val jsonElement =
        Json.parseToJsonElement("""{"text" : "Hello world this is some text", "done": true, "something": null, "array": [{ "a": "b"},1,2,3,4,5] }""")

    @Test
    fun `ProtoBuf vs sqlite jsonb`() = runTest {
        val db = Database.inMemorySingleConnection()
        val protobuf = ProtoBuf {}
        val encodedSqliteJsonb = db.read { select("SELECT jsonb(?)", jsonElement.toString()).first { getBlob(0) } }
        val encodedProtobuf = protobuf.encodeToByteArray(JsonElementAsStringSerializer, jsonElement)
        println("Protobuf (string): ${encodedProtobuf.size} bytes - ${encodedProtobuf.toHexString()}")
        println("Jsonb: ${encodedSqliteJsonb.size} bytes - ${encodedSqliteJsonb.toHexString()}")
    }

    @Test
    fun `ProtoBuf should be best packing option`() = runTest {
        val module = SerializersModule {
            contextual(JsonElement::class, JsonElementAsStringSerializer)
        }
        val protobuf = ProtoBuf { serializersModule = module }
        val cbor = Cbor { serializersModule = module }
        val json =
            Json { serializersModule = SerializersModule { contextual(JsonElement::class, JsonElement.serializer()) } }
        val db = Database.inMemorySingleConnection()
        val action = TestAction("some text", byteArrayOf(0, 1, 2, 3, 4, 5, 6), Uuid.random(), jsonElement)
        val actionSerializer = PolymorphicIntSerializer(mapOf(2 to (TestAction::class to TestAction.serializer())))

        val encodedProtobuf = protobuf.encodeToByteArray(actionSerializer, action)
        val encodedJson = json.encodeToString(actionSerializer, action)
        val encodedSqliteJsonb = db.read { select("SELECT jsonb(?)", encodedJson).first { getBlob(0) } }
        val encodedCbor = cbor.encodeToByteArray(actionSerializer, action)
        val decoded = protobuf.decodeFromByteArray(actionSerializer, encodedProtobuf)

        println("Encoded: $encodedJson")
        println("Protobuf: ${encodedProtobuf.size} bytes - ${encodedProtobuf.toHexString()}")
        println("Jsonb: ${encodedSqliteJsonb.size} bytes - ${encodedSqliteJsonb.toHexString()}")
        println("Cbor: ${encodedCbor.size} bytes - ${encodedCbor.toHexString()}")
        println("Decoded: $decoded")

        encodedProtobuf.size shouldBeLessThan encodedCbor.size
        encodedProtobuf.size shouldBeLessThan encodedSqliteJsonb.size
    }
}