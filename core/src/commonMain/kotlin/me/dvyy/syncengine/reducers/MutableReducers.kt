package me.dvyy.syncengine.reducers

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializer
import me.dvyy.sqlite.WriteTransaction
import me.dvyy.syncengine.actions.Action
import kotlin.reflect.KClass

fun reducers(block: MutableReducers.() -> Unit) = MutableReducers().apply(block).build()
class MutableReducers {
    val mutators = mutableMapOf<KClass<out Action>, context(WriteTransaction) (Action) -> Unit>()

    inline fun <reified T : Action> reduce(noinline run: context(WriteTransaction) (T) -> Unit) {
        mutators[T::class] = run as context(WriteTransaction) (Action) -> Unit
    }

    @OptIn(InternalSerializationApi::class)
    fun build(): Reducers {
        val serializersModule = SerializersModule {
            polymorphic(Action::class) {
                mutators.keys.forEach {
                    subclass(it as KClass<Action>, it.serializer())
                }
            }
        }
        return Reducers(serializersModule, mutators.toMap())
    }
}

