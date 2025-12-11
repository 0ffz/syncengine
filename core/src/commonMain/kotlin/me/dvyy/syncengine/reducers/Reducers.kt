package me.dvyy.syncengine.reducers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import me.dvyy.sqlite.WriteTransaction
import me.dvyy.syncengine.actions.Action
import kotlin.reflect.KClass

class Reducers(
    val serializersModule: SerializersModule,
    val actionsToReducers: Map<KClass<*>, context(WriteTransaction) (Action) -> Unit>,
    val idsToSerializers: Map<Int, Pair<KClass<out Action>, KSerializer<out Action>>>,
)