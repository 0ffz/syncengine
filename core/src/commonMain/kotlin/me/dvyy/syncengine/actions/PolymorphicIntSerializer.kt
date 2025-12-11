package me.dvyy.syncengine.actions

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.*
import me.dvyy.syncengine.reducers.ActionDefinition
import me.dvyy.syncengine.schema.Schema
import kotlin.reflect.KClass

/**
 * Serializes subclasses of [Action] via a user-defined integer field.
 * Formats like protobuf can compress this to very few bytes of overhead,
 * better than String SerialNames
 */
@OptIn(InternalSerializationApi::class)
class PolymorphicIntSerializer(
    val subclassSerializers: Map<Int, ActionDefinition>,
) : KSerializer<Action> {
    val classToSerializers: Map<KClass<out Action>, KSerializer<out Action>> =
        subclassSerializers.values.associate { it.actionClass to it.serializer }
    val classToId: Map<KClass<out Action>, Int> = subclassSerializers.map { it.value.actionClass to it.key }.toMap()

    override val descriptor: SerialDescriptor = run {
        buildClassSerialDescriptor("me.dvyy.syncengine.PolymorphicIntSerializer") {
            element("type", Int.serializer().descriptor)
            element(
                "value",
                buildSerialDescriptor("me.dvyy.syncengine.PolymorphicIntSerializer", SerialKind.CONTEXTUAL)
            )
        }
//        buildSerialDescriptor("me.dvyy.syncengine.PolymorphicIntSerializer", PolymorphicKind.OPEN) {
//            element("type", Int.serializer().descriptor)
//            element(
//                "value",
//                buildSerialDescriptor("me.dvyy.syncengine.PolymorphicIntSerializer", SerialKind.CONTEXTUAL)
//            )
//        }
    }

    override fun serialize(encoder: Encoder, value: Action) {
        val actualSerializer = classToSerializers[value::class]
        val id = classToId[value::class]
        ?: error("Class ${value::class} doesn't have a known id!")
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, 0, id)
            encodeSerializableElement(descriptor, 1, actualSerializer as KSerializer<Action>, value)
        }
    }

    override fun deserialize(decoder: Decoder): Action {
        return decoder.decodeStructure(descriptor) {
            var decodedInt: Int? = null
            var decodedValue: Action? = null

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> decodedInt = decodeIntElement(descriptor, 0)
                    1 -> {
                        val serializer = subclassSerializers[decodedInt]?.serializer
                            ?: error("Unknown type id: $decodedInt")
                        decodedValue = decodeSerializableElement(descriptor, 1, serializer as KSerializer<Action>)
                    }

                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }

            decodedValue ?: error("Missing value")
        }
    }

    companion object {
        fun of(schema: Schema): PolymorphicIntSerializer =
            PolymorphicIntSerializer(schema.protocol.actionDefinitions)
    }
}