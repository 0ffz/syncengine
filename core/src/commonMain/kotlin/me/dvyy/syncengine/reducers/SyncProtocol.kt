package me.dvyy.syncengine.reducers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import me.dvyy.syncengine.actions.Action
import kotlin.reflect.KClass

data class ActionDefinition(val actionClass: KClass<out Action>, val serializer: KSerializer<out Action>)

class SyncProtocolBuilder {
    @PublishedApi
    internal val actionDefinitions = mutableMapOf<Int, ActionDefinition>()

    @PublishedApi
    internal val registeredActions = mutableListOf<KClass<out Action>>()

    inline fun <reified T : Action> action(
        id: Int,
        serializer: KSerializer<T> = serializer<T>(),
    ) {
        val kClass = T::class
        if (kClass in registeredActions) error("Action already registered: $kClass")
        if (id in actionDefinitions.keys) error("Duplicate id $id for action $kClass, already registered as ${actionDefinitions[id]?.actionClass}")
        actionDefinitions[id] = ActionDefinition(kClass, serializer)
        registeredActions += kClass
    }

    fun build() = SyncProtocol(actionDefinitions)
}

data class SyncProtocol(val actionDefinitions: Map<Int, ActionDefinition>)

fun syncProtocol(block: SyncProtocolBuilder.() -> Unit) = SyncProtocolBuilder().apply(block).build()
