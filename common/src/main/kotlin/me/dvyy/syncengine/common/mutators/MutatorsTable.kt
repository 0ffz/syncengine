package me.dvyy.syncengine.common.mutators

import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.json.json

object MutatorsTable : IntIdTable() {
    var data = json("data", Json.Default, Mutator.serializer())
}