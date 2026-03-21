package me.dvyy.syncengine.server.schema

import me.dvyy.sqlite.WriteTransaction
import me.dvyy.syncengine.schema.JsonTable

class UserRestrictedJsonTable(from: JsonTable) : JsonTable(from.name, from.indexes) {
    context(tx: WriteTransaction)
    override fun create() {
        tx.exec(
            """
            CREATE TABLE IF NOT EXISTS $name (
                id BLOB NOT NULL PRIMARY KEY,
                data BLOB,
                owner INTEGER NOT NULL,
                frame INTEGER
            ) STRICT;
            """.trimIndent()
        )
        // Create update trigger that sets frame to current time
        tx.exec(
            """
                CREATE TRIGGER IF NOT EXISTS ${name}_frame_update 
                AFTER UPDATE ON $name 
                FOR EACH ROW 
                BEGIN
                    UPDATE $name SET frame = (SELECT value FROM syncengine_store WHERE key = 'frame') WHERE id = new.id;
                END;
            """.trimIndent()
        )
        tx.exec(
            """
                CREATE TRIGGER IF NOT EXISTS ${name}_frame_insert 
                AFTER INSERT ON $name 
                FOR EACH ROW 
                BEGIN
                    UPDATE $name SET frame = (SELECT value FROM syncengine_store WHERE key = 'frame') WHERE id = new.id;
                END;
            """.trimIndent()
        )
        super.createIndexes()
    }
//    context(tx: WriteTransaction)
//    fun createIndexes() {
//        tx.exec("CREATE INDEX IF NOT EXISTS ${name}_owner ON $name(owner);")
//        tx.exec(
//            """
//            CREATE TRIGGER IF NOT EXISTS ${name}_frame_update
//                BEFORE UPDATE ON $name
//                FOR EACH ROW
//            BEGIN
//                UPDATE $name SET frame = (strftime('%s','now') || substr(strftime('%f','now'),4))
//                WHERE id = old.id;
//            END;
//        """.trimIndent()
//        )
//    }
}
