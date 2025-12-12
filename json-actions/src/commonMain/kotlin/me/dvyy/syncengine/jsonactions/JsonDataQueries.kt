package me.dvyy.syncengine.jsonactions

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import me.dvyy.sqlite.Transaction
import me.dvyy.sqlite.WriteTransaction
import me.dvyy.sqlite.statement.NamedColumnSqliteStatement
import me.dvyy.syncengine.schema.JsonTable
import org.intellij.lang.annotations.Language
import kotlin.uuid.Uuid

/**
 * CRUD operations for [me.dvyy.syncengine.schema.JsonTable].
 *
 * @property table The table to perform crud operations on.
 */
class JsonDataQueries<T>(
    val serializer: KSerializer<T>,
    val table: JsonTable,
    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    },
) {
    context(tx: Transaction)
    fun get(
        id: Uuid,
    ): T? = tx.getOrNull("SELECT json(data) FROM $table WHERE id = ? AND data IS NOT null", id) {
        json.decodeFromString(serializer, getText(0))
    }

    context(tx: Transaction)
    fun <T> jsonGet(
        id: Uuid,
        jsonPath: String,
        statement: NamedColumnSqliteStatement.() -> T,
    ): T? = tx.getOrNull("SELECT json(data) FROM $table WHERE id = ? AND data IS NOT null", id) {
        statement()
    }

    context(tx: Transaction)
    fun getJsonElement(id: Uuid) = tx.getSingle("SELECT json(data) FROM $table WHERE id = ? AND data IS NOT null", id) {
        json.parseToJsonElement(getText(0))
    }

    context(tx: WriteTransaction)
    fun create(
        id: Uuid,
        data: JsonElement,
    ) {
        tx.exec("INSERT INTO $table (id, data, owner) VALUES (?, jsonb(?), ?)", id, data.toString(), tx.identity)
    }

    context(tx: WriteTransaction)
    fun patch(
        id: Uuid,
        data: T,
    ) = patch(id, json.encodeToString(serializer, data))

    context(tx: WriteTransaction)
    fun delete(id: Uuid) {
        tx.exec("UPDATE $table SET data = null WHERE id = ? AND owner = ?", id, tx.identity)
    }

    context(tx: WriteTransaction)
    fun jsonSet(id: Uuid, path: String, value: String) {
        tx.exec(
            "UPDATE $table SET data = jsonb_set(data, ?, jsonb(?)) WHERE id = ? AND owner = ?",
            path, value, id, tx.identity
        )
    }

    context(tx: WriteTransaction)
    fun patch(
        id: Uuid,
        @Language("JSON") patchString: String,
    ) {
        tx.exec(
            """
            UPDATE $table SET
            data = jsonb_patch(data, jsonb(?))
            WHERE id = ? AND owner = ?
            """.trimIndent(),
            patchString, id, tx.identity
        )
    }
}