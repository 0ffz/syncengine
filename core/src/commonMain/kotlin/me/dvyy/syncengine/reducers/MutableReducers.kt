package me.dvyy.syncengine.reducers

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializer
import me.dvyy.sqlite.WriteTransaction
import me.dvyy.syncengine.actions.Action
import kotlin.reflect.KClass

fun reducers(block: MutableReducers.() -> Unit) = MutableReducers().apply(block).build()

class MutableReducers {
    val actionsToReducers = mutableMapOf<KClass<out Action>, context(WriteTransaction) (Action) -> Unit>()
    val idsToSerializers = mutableMapOf<Int, Pair<KClass<out Action>, KSerializer<out Action>>>()

    inline fun <reified T : Action> reduce(
        id: Int,
        serializer: KSerializer<T> = serializer<T>(),
        noinline run: context(WriteTransaction) (T) -> Unit,
    ) {
        val kClass = T::class
        if (kClass in actionsToReducers) error("Reducer already registered for action type $kClass")
        actionsToReducers[kClass] = run as context(WriteTransaction) (Action) -> Unit
        if (id in idsToSerializers) error("Duplicate id $id for action $kClass")
        idsToSerializers[id] = kClass to serializer
    }

    @OptIn(InternalSerializationApi::class)
    fun build(): Reducers {
        val serializersModule = SerializersModule {
            polymorphic(Action::class) {
                actionsToReducers.keys.forEach {
                    subclass(it as KClass<Action>, it.serializer())
                }
            }
        }
        return Reducers(serializersModule, actionsToReducers.toMap(), idsToSerializers.toMap())
    }
}

