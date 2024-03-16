package com.github.encryptsl.credit.common.database

import org.bukkit.Bukkit
import org.jetbrains.exposed.sql.SqlLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.transactions.TransactionManager

class SqlPluginLogger : SqlLogger {

    private val plugin = Bukkit.getPluginManager().getPlugin("CreditLite")
    override fun log(context: StatementContext, transaction: Transaction) {
        if (plugin?.config?.getBoolean("database.sql-plugin-logger") != true) return
        plugin.logger.info(context.sql(TransactionManager.current()))
    }
}