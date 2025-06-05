@file:OptIn(ExperimentalTime::class)

package me.dvyy.syncengine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import me.dvyy.syncengine.common.RowDiff
import me.dvyy.syncengine.common.SyncResult
import me.dvyy.syncengine.common.mutators.Mutator
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.r2dbc.*
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class ServerDataStore(
    val store: me.dvyy.syncengine.common.KeyValueStore = SqlBackedKeyValueStore(),
) {
    val db = R2dbcDatabase.connect("r2dbc:h2:mem:///regular;DB_CLOSE_DELAY=-1;")

    init {
        CoroutineScope(Dispatchers.Default).launch {
            suspendTransaction {
                SchemaUtils.create(KeyValueTable)
            }
        }
    }

    suspend fun apply(mutator: Mutator) = mutator.mutate(store)

    suspend fun apply(mutators: List<Mutator>) = mutators.forEach {
        apply(it)
    }

    suspend fun getUpdatedSince(timestamp: Long): SyncResult.Updates = suspendTransaction {
        val updates = KeyValueTable.select(KeyValueTable.id, KeyValueTable.value)
            .where { KeyValueTable.editTime greater timestamp }
            .map {
                RowDiff(
                    it[KeyValueTable.id].value,
                    it[KeyValueTable.value]
                )
            }
            .toList()
        val lastUpdate = KeyValueTable.select(KeyValueTable.editTime)
            .lastOrNull()
            ?.get(KeyValueTable.editTime)
            ?: timestamp
        SyncResult.Updates(
            updates,
            lastUpdate
        )
    }
}

class SqlBackedKeyValueStore : me.dvyy.syncengine.common.KeyValueStore {
    override suspend fun set(key: Long, value: String?) {
        if (value == null) remove(key)
        else KeyValueTable.upsert(KeyValueTable.id) { it[id] = key; it[this.value] = value; it[editTime] = System.currentTimeMillis() }
    }

    override suspend fun get(key: Long): String? {
        return KeyValueTable.select(KeyValueTable.id eq key, KeyValueTable.value).firstOrNull()
            ?.get(KeyValueTable.value)
    }

    override suspend fun remove(key: Long) {
        KeyValueTable.deleteWhere { KeyValueTable.id eq key }
    }
}

object KeyValueTable : LongIdTable() {
    val value = text("value")
    val editTime = long("timestamp")//datetime("timestamp").defaultExpression(CurrentDateTime)
}
