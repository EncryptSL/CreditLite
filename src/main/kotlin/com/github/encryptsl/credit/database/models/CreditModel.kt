package com.github.encryptsl.credit.database.models

import com.github.encryptsl.credit.api.interfaces.DatabaseSQLProvider
import com.github.encryptsl.credit.database.tables.Account
import org.bukkit.Bukkit
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class CreditModel : DatabaseSQLProvider {

    override fun createPlayerAccount(username: String, uuid: UUID, credit: Double) {
        transaction {
            Account.insertIgnore {
                it[Account.username] = username
                it[Account.uuid] = uuid.toString()
                it[Account.credit] = credit
            }
        }
    }

    override fun deletePlayerAccount(uuid: UUID) {
        transaction {
            Account.deleteWhere { Account.uuid eq uuid.toString() }
        }
    }

    override fun getExistPlayerAccount(uuid: UUID): Boolean = transaction {
        !Account.select(Account.uuid).where(Account.uuid eq uuid.toString()).empty()
    }

    override fun getTopBalance(top: Int): MutableMap<String, Double> = transaction {
        Account.selectAll().limit(top).associate {
            it[Account.uuid] to it[Account.credit]
        }.toMutableMap()
    }

    override fun getTopBalance(): MutableMap<String, Double> = transaction {
        Account.selectAll().associate {
            it[Account.uuid] to it[Account.credit]
        }.toMutableMap()
    }

    override fun getBalance(uuid: UUID): Double = transaction {
        Account.select(Account.uuid, Account.uuid).where(Account.uuid eq uuid.toString()).first()[Account.credit]
    }

    override fun depositCredit(uuid: UUID, credit: Double) {
        transaction {
            Account.update({ Account.uuid eq uuid.toString() }) {
                it[Account.credit] = Account.credit plus credit
            }
        }
    }
    override fun withdrawCredit(uuid: UUID, credit: Double) {
        transaction {
            Account.update({ Account.uuid eq uuid.toString() }) {
                it[Account.credit] = Account.credit minus credit
            }
        }
    }
    override fun setCredit(uuid: UUID, credit: Double) {
        transaction {
            Account.update({ Account.uuid eq uuid.toString() }) {
                it[Account.credit] = credit
            }
        }
    }

    override fun purgeAccounts() {
        transaction { Account.deleteAll() }
    }

    override fun purgeDefaultAccounts(defaultCredit: Double) {
        transaction { Account.deleteWhere { credit eq defaultCredit } }
    }

    override fun purgeInvalidAccounts() {
        val validPlayerUUIDs = Bukkit.getOfflinePlayers().mapNotNull { runCatching { it.uniqueId }.getOrNull() }.map { it.toString() }
        transaction {
            Account.deleteWhere {
                uuid notInList validPlayerUUIDs
            }
        }
    }
}