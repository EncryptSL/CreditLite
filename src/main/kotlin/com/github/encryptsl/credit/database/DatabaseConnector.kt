package com.github.encryptsl.credit.database

import com.github.encryptsl.credit.api.interfaces.DatabaseConnectorProvider
import com.github.encryptsl.credit.database.tables.Account
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

class DatabaseConnector : DatabaseConnectorProvider {

    override fun initConnect(jdbcHost: String, user: String, pass: String) {
        val config = HikariDataSource().apply {
            maximumPoolSize = 10
            jdbcUrl = jdbcHost
            username = user
            password = pass
        }

        Database.connect(config)

        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Account)
        }
    }
}