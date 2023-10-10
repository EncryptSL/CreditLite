package com.github.encryptsl.kredit.database.models

import com.github.encryptsl.kredit.api.interfaces.DatabaseSQLProvider
import com.github.encryptsl.kredit.database.tables.Account
import org.bukkit.Bukkit
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class CreditModel : DatabaseSQLProvider {

    override fun createPlayerAccount(uuid: UUID, credit: Double) {
        transaction {
            Account.insertIgnore {
                it[Account.uuid] = uuid.toString()
                it[Account.money] = credit
            }
        }
    }

    override fun deletePlayerAccount(uuid: UUID) {
        transaction {
            Account.deleteWhere { Account.uuid eq uuid.toString() }
        }
    }

    override fun getExistPlayerAccount(uuid: UUID): Boolean = transaction {
        !Account.select(Account.uuid eq uuid.toString()).empty()
    }

    override fun getTopBalance(top: Int): MutableMap<String, Double> = transaction {
        Account.selectAll().limit(top).associate {
            it[Account.uuid] to it[Account.money]
        }.toMutableMap()
    }

    override fun getTopBalance(): MutableMap<String, Double> = transaction {
        Account.selectAll().associate {
            it[Account.uuid] to it[Account.money]
        }.toMutableMap()
    }

    override fun getBalance(uuid: UUID): Double = transaction {
        Account.select(Account.uuid eq uuid.toString()).first()[Account.money]
    }

    override fun depositCredit(uuid: UUID, credit: Double) {
        transaction {
            Account.update({ Account.uuid eq uuid.toString() }) {
                it[Account.money] = Account.money plus credit
            }
        }
    }
    override fun withdrawCredit(uuid: UUID, credit: Double) {
        transaction {
            Account.update({ Account.uuid eq uuid.toString() }) {
                it[Account.money] = Account.money minus credit
            }
        }
    }
    override fun setCredit(uuid: UUID, money: Double) {
        transaction {
            Account.update({ Account.uuid eq uuid.toString() }) {
                it[Account.money] = money
            }
        }
    }

    override fun purgeAccounts() {
        transaction { Account.deleteAll() }
    }

    override fun purgeDefaultAccounts(defaultMoney: Double) {
        transaction { Account.deleteWhere { money eq defaultMoney } }
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