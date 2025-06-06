package me.dvyy.syncengine.common

import me.dvyy.syncengine.common.ui.QueryObserver
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.statements.DeleteStatement
import org.jetbrains.exposed.v1.core.statements.GlobalStatementInterceptor
import org.jetbrains.exposed.v1.core.statements.StatementContext
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.core.statements.api.PreparedStatementApi

object CustomInterceptor: GlobalStatementInterceptor {
    val editedTables = mutableSetOf<Table>()
    val listeners = mutableMapOf<String, MutableSet<QueryObserver>>()
//    override fun afterCommit(transaction: Transaction) {
//    }
    override fun beforeExecution(transaction: Transaction, context: StatementContext) {
        println("Before execution ${context.statement.type}")

        when(val statement = context.statement) {
            is DeleteStatement, is UpdateBuilder<*> -> {
                println("Edited " + statement.targets.joinToString(" ") { it.tableName })
                editedTables += statement.targets
            }
        }
    }

    override fun afterStatementPrepared(transaction: Transaction, preparedStatement: PreparedStatementApi) {
        println("After prepare $preparedStatement")
    }

    override fun afterCommit(transaction: Transaction) {
        editedTables.forEach { table ->
            listeners[table.tableName]?.forEach {
                it.notifyUpdate()
            }
        }
        println("Notifying changes to $editedTables")
        editedTables.clear()
    }
}