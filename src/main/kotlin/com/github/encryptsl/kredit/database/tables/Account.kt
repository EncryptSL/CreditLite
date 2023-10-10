package com.github.encryptsl.kredit.database.tables

import org.jetbrains.exposed.sql.Table

object Account : Table() {
    private val id = integer( "id").autoIncrement()
    val uuid = varchar("uuid", 36)
    val credit = double("credit")

    override val primaryKey: PrimaryKey = PrimaryKey(id)

    override val tableName: String
        get() = "credits"
}