package me.dvyy.syncengine.db.tables

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import me.dvyy.syncengine.db.Transaction
import me.dvyy.syncengine.db.WriteTransaction
import me.dvyy.syncengine.db.bindUuid
import org.intellij.lang.annotations.Language
import java.util.*

class JsonTableDAO<T>(
    val serializer: KSerializer<T>,
    val table: String,
    val json: Json = Json,
) {
    context(tx: Transaction)
    fun get(
        id: UUID,
    ): T = tx.prepare("SELECT json(data) FROM $table WHERE id = ?") {
        bindUuid(1, id)
        step()
        json.decodeFromString(serializer, getText(0))
    }

    context(tx: WriteTransaction)
    fun mutate(
        id: UUID,
        data: T,
    ) = mutate(id, json.encodeToString(serializer, data))

    context(tx: WriteTransaction)
    fun mutate(
        id: UUID,
        @Language("JSON") patchString: String,
    ) {
        tx.prepare(
            """
            INSERT INTO $table(id, data)
            VALUES (?, jsonb(?)) 
            ON CONFLICT DO UPDATE SET 
            data = jsonb_patch(data, jsonb(excluded.data))
            """.trimIndent()
        ) {
            bindUuid(1, id)
            bindText(2, patchString)
            step()
        }
    }
}
