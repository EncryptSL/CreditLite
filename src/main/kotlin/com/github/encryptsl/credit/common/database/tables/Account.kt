package com.github.encryptsl.credit.common.database.tables

import org.jetbrains.exposed.sql.Table


object Account : Table("credits") {
    private val id = integer( "id").autoIncrement()
    val username = varchar("username", 36)
    val uuid = varchar("uuid", 36)
    val credit = decimal("credit", 1, 2)

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}