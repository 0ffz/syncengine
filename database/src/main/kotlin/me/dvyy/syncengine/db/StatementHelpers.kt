package me.dvyy.syncengine.db

import androidx.sqlite.SQLiteStatement
import java.util.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

@OptIn(ExperimentalUuidApi::class)
fun SQLiteStatement.bindUuid(index: Int, uuid: UUID) = bindBlob(index, uuid.toKotlinUuid().toByteArray())

fun SQLiteStatement.bindAny(index: Int, value: Any) = when (value) {
    is Int -> bindInt(index, value)
    is Long -> bindLong(index, value)
    is String -> bindText(index, value)
    is Boolean -> bindBoolean(index, value)
    is Float -> bindFloat(index, value)
    is Double -> bindDouble(index, value)
    is ByteArray -> bindBlob(index, value)
    is UUID -> bindUuid(index, value)
    else -> error("Could not bind any of type ${value::class.simpleName}")
}
