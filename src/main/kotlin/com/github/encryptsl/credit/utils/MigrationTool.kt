package com.github.encryptsl.credit.utils

import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MigrationTool(private val creditLite: com.github.encryptsl.credit.CreditLite) {

    fun migrateToCSV(data: List<MigrationData>, fileName: String): Boolean {
        val file = File("${creditLite.dataFolder}/migration/", "${fileName}_${dateTime()}.csv")

        return try {
            file.parentFile.mkdirs()
            PrintWriter(FileWriter(file)).use { writer ->
                writer.println("id,uuid, username,credit")
                data.forEach {
                    writer.println("${it.id},${it.uuid},${it.money}")
                }
            }
            true
        }catch (e: IOException) {
            creditLite.logger.severe("Error while migrating to CSV file: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    fun migrateToSQL(data: List<MigrationData>, fileName: String): Boolean {
        val file = File("${creditLite.dataFolder}/migration/", "${fileName}_${dateTime()}.sql")

        return try {
            file.parentFile.mkdirs()
            PrintWriter(FileWriter(file)).use { writer ->
                writer.println("DROP TABLE IF EXISTS credits;")
                writer.println("CREATE TABLE credits (id INT, uuid VARCHAR(36), username VARCHAR(16), credit DOUBLE);")
                val insertStatements = data.joinToString {
                    "\n(${it.id}, '${it.uuid}', ${it.money})"
                }
                writer.println("INSERT INTO credits (id, uuid, username, credit) VALUES $insertStatements;")
            }
            true
        } catch (e: IOException) {
            creditLite.logger.severe("Error while migrating to SQL file: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    private fun dateTime(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm")
        return LocalDateTime.now().format(formatter)
    }
}

data class MigrationData(val id: Int, val uuid: String, val money: Double)
