package me.dvyy.syncengine.db.tables

import me.dvyy.syncengine.db.Transaction
import me.dvyy.syncengine.db.WriteTransaction
import java.util.*

class RelationTableDAO<T> {
    context(tx: Transaction)
    fun relatedTo(uuid: UUID): List<UUID> = tx.getList("SELECT child FROM subtask") {
        UUID.nameUUIDFromBytes(getBlob(0))
    }

    context(tx: WriteTransaction)
    fun moveAfter(
        parent: UUID,
        child: UUID,
        other: UUID,
    ) {
        //TODO better binds
        val rank = tx.getSingle(
            "SELECT rank FROM subtask WHERE parent = ? and child = ?",
            parent, other
        ) { getText(0) }
        val next = tx.getSingle(
            "SELECT rank FROM subtask WHERE parent = ? and rank > ?",
            parent, rank
        ) { getText(0) }
        val middle: String = TODO()
        tx.getSingle(
            "UPDATE subtask SET rank = ?, parent = ? WHERE parent = ? AND child = ?",
            middle, parent, parent, other
        ) {

        }
    }
}

