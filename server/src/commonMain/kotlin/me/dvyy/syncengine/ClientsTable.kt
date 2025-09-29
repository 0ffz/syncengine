package me.dvyy.syncengine

import me.dvyy.syncengine.schema.jsonTable
import me.dvyy.syncengine.schema.view

val ClientsTable = jsonTable("clients")

val ClientsView = view("clients_view", ClientsTable) {
    blob("uuid")
    integer("last_mutator_applied")
}
