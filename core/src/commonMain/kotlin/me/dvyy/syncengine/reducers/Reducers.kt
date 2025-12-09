package me.dvyy.syncengine.reducers

import kotlinx.serialization.modules.SerializersModule
import me.dvyy.sqlite.WriteTransaction
import me.dvyy.syncengine.actions.Action
import kotlin.reflect.KClass

class Reducers(
    val serializersModule: SerializersModule,
    val actions: Map<KClass<*>, context(WriteTransaction) (Action) -> Unit>,
)