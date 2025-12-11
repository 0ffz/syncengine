package me.dvyy.syncengine.reducers

import kotlinx.serialization.InternalSerializationApi
import me.dvyy.sqlite.WriteTransaction
import me.dvyy.syncengine.actions.Action
import kotlin.reflect.KClass

fun reducers(block: MutableReducers.() -> Unit) = MutableReducers().apply(block).build()

class MutableReducers {
    val actionsToReducers = mutableMapOf<KClass<out Action>, context(WriteTransaction) (Action) -> Unit>()

    inline fun <reified T : Action> reduce(
        noinline run: context(WriteTransaction) (T) -> Unit,
    ) {
        val kClass = T::class
        if (kClass in actionsToReducers) error("Reducer already registered for action type $kClass")
        actionsToReducers[kClass] = run as context(WriteTransaction) (Action) -> Unit
    }

    @OptIn(InternalSerializationApi::class)
    fun build(): Reducers {
        return Reducers(actionsToReducers.toMap())
    }
}

