package me.dvyy.syncengine.db.tables

import kotlinx.serialization.json.Json
import me.dvyy.syncengine.db.Transaction
import me.dvyy.syncengine.db.WriteTransaction

object MutatorsTable : Table(
    """
    CREATE TABLE IF NOT EXISTS mutators(
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        data BLOB NOT NULL
    )
    """.trimIndent()
) {
    fun Transaction.getAllEncoded() = getList("SELECT data FROM mutators") { getBlob(0) }

    context(tx: Transaction)
    fun forEachMutator() {
        tx.forEach("SELECT json(data) FROM mutators") {
            Json.decodeFromString<String>(getText(0)) //TODO mutator
        }
    }

    context(tx: WriteTransaction)
    fun append(mutator: String) {
        tx.exec("INSERT INTO mutators(data) VALUES (jsonb(?))", mutator)
    }
}
