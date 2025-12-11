package me.dvyy.syncengine.actions

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.*
import me.dvyy.syncengine.reducers.Reducers
import kotlin.reflect.KClass

/**
 * Serializes subclasses of type [T] via a user-defined integer field.
 * Formats like protobuf can compress this to very few bytes of overhead,
 * better than String SerialNames
 */
@OptIn(InternalSerializationApi::class)
class PolymorphicIntSerializer<T : Any>(
    val subclassSerializers: Map<Int, Pair<KClass<out T>, KSerializer<out T>>>,
    val default: Int = -1,
) : KSerializer<T> {
    val classToSerializers: Map<KClass<out T>, KSerializer<out T>> =
        subclassSerializers.values.associate { it.first to it.second }
    val classToId: Map<KClass<out T>, Int> = subclassSerializers.map { it.value.first to it.key }.toMap()

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

    override fun serialize(encoder: Encoder, value: T) {
        val actualSerializer = classToSerializers[value::class] ?: subclassSerializers[default]?.second
        val id = classToId[value::class] ?: default.takeIf { it != -1 }
        ?: error("Class ${value::class} doesn't have a known id!")
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, 0, id)
            encodeSerializableElement(descriptor, 1, actualSerializer as KSerializer<T>, value)
        }
    }

    override fun deserialize(decoder: Decoder): T {
        return decoder.decodeStructure(descriptor) {
            var decodedInt: Int? = null
            var decodedValue: T? = null

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> decodedInt = decodeIntElement(descriptor, 0)
                    1 -> {
                        val serializer = subclassSerializers[decodedInt]?.second
                            ?: error("Unknown type id: $decodedInt")
                        decodedValue = decodeSerializableElement(descriptor, 1, serializer as KSerializer<T>)
                    }

                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }

            decodedValue ?: error("Missing value")
        }
    }

    companion object {
        fun of(reducers: Reducers): PolymorphicIntSerializer<Action> =
            PolymorphicIntSerializer(reducers.idsToSerializers)
    }
}