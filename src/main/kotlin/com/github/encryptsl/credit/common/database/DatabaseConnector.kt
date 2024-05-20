package com.github.encryptsl.credit.common.database

import com.github.encryptsl.credit.CreditLite
import com.github.encryptsl.credit.common.database.tables.Account
import com.github.encryptsl.credit.common.database.tables.MonologTable
import com.github.encryptsl.credit.common.extensions.loggedTransaction
import com.github.encryptsl.kmono.lib.api.database.DatabaseBuilder
import com.github.encryptsl.kmono.lib.api.database.DatabaseConnectorProvider
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.SchemaUtils

class DatabaseConnector(private val creditLite: CreditLite) : DatabaseConnectorProvider {

    override fun createConnection(jdbcHost: String, user: String, pass: String) {
        DatabaseBuilder.Builder()
            .setJdbc(jdbcHost)
            .setUser(user)
            .setPassword(pass)
            .setLogger(creditLite.slF4JLogger)
            .setConnectionPool(10)
            .setDatasource(HikariDataSource())
            .connect()

        loggedTransaction {
            SchemaUtils.create(Account, MonologTable)
        }
    }
}