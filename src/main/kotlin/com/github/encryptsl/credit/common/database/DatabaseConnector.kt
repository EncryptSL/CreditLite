package com.github.encryptsl.credit.common.database

import com.github.encryptsl.credit.api.interfaces.DatabaseConnectorProvider
import com.github.encryptsl.credit.common.database.tables.Account
import com.github.encryptsl.credit.common.extensions.loggedTransaction
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils

class DatabaseConnector : DatabaseConnectorProvider {

    override fun initConnect(jdbcHost: String, user: String, pass: String) {
        val config = HikariDataSource().apply {
            maximumPoolSize = 10
            jdbcUrl = jdbcHost
            username = user
            password = pass
        }

        Database.connect(config)

        loggedTransaction {
            SchemaUtils.create(Account)
        }
    }
}