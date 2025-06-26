package me.dvyy.syncengine.schema

import me.dvyy.syncengine.db.WriteTransaction

interface AbstractMutator<T> {
    context(tx: WriteTransaction)
    fun mutate(db: T)

    fun reduce(previous: AbstractMutator<T>): AbstractMutator<T>? = null
}
