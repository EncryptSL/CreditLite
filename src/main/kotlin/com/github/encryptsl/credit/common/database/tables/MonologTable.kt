package com.github.encryptsl.credit.common.database.tables

import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import java.util.logging.Level

object MonologTable : Table("credits_monolog") {
    private val id = integer( "id").autoIncrement()
    val level = varchar("level", 8).default(Level.INFO.name)
    val log = text("log")
    val timestamp = timestamp("timestamp").default(Clock.System.now())

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}