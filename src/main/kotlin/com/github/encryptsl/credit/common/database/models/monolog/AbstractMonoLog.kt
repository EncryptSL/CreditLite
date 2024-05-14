package com.github.encryptsl.credit.common.database.models.monolog

import com.github.encryptsl.credit.api.interfaces.AdapterLogger
import com.github.encryptsl.credit.common.database.entity.EconomyLog
import com.github.encryptsl.credit.common.database.tables.MonologTable
import com.github.encryptsl.credit.common.extensions.loggedTransaction
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.util.concurrent.CompletableFuture
import java.util.logging.Level
import kotlin.collections.mapNotNull

abstract class AbstractMonoLog(private val isEnabledMonolog: Boolean) : AdapterLogger {

    override fun getLog(): CompletableFuture<List<EconomyLog>> {
        val future = CompletableFuture<List<EconomyLog>>()

        val economyLog = loggedTransaction {
            return@loggedTransaction MonologTable.selectAll().orderBy(MonologTable.timestamp, SortOrder.DESC).mapNotNull {
                EconomyLog(
                    it[MonologTable.level],
                    it[MonologTable.log],
                    it[MonologTable.timestamp]
                )
            }
        }
        future.completeAsync { economyLog }
        return future
    }

    override fun clearLogs() {
        loggedTransaction { MonologTable.deleteAll() }
    }

    protected fun log(level: Level, message: String) {
        if (!isEnabledMonolog) return
        loggedTransaction {
            MonologTable.insert {
                it[MonologTable.level] = level.name
                it[log] = message
            }
        }
    }
}