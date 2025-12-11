package me.dvyy.syncengine.reducers

import me.dvyy.sqlite.WriteTransaction
import me.dvyy.syncengine.actions.Action
import kotlin.reflect.KClass

class Reducers(
    val actionsToReducers: Map<KClass<*>, context(WriteTransaction) (Action) -> Unit>,
)