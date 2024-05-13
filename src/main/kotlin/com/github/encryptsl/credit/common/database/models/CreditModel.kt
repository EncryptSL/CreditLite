package com.github.encryptsl.credit.common.database.models

import com.github.encryptsl.credit.api.interfaces.DatabaseSQLProvider
import com.github.encryptsl.credit.common.database.tables.Account
import com.github.encryptsl.credit.common.extensions.loggedTransaction
import org.bukkit.Bukkit
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import java.util.*
import java.util.concurrent.CompletableFuture

class CreditModel : DatabaseSQLProvider {

    override fun createPlayerAccount(username: String, uuid: UUID, credit: Double) {
        loggedTransaction {
            Account.insertIgnore {
                it[Account.username] = username
                it[Account.uuid] = uuid.toString()
                it[Account.credit] = credit
            }
        }
    }

    override fun deletePlayerAccount(uuid: UUID) {
        loggedTransaction {
            Account.deleteWhere { Account.uuid eq uuid.toString() }
        }
    }

    override fun getExistPlayerAccount(uuid: UUID): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()

        loggedTransaction {
            future.completeAsync {
                !Account.select(Account.uuid).where(Account.uuid eq uuid.toString()).empty()
            }
        }

        return future
    }

    override fun getTopBalance(top: Int): MutableMap<String, Double> = loggedTransaction {
        Account.selectAll().limit(top).orderBy(Account.credit, SortOrder.DESC).associate {
            it[Account.uuid] to it[Account.credit]
        }.toMutableMap()
    }

    override fun getTopBalance(): MutableMap<String, Double> = loggedTransaction {
        Account.selectAll().orderBy(Account.credit, SortOrder.DESC).associate {
            it[Account.uuid] to it[Account.credit]
        }.toMutableMap()
    }

    override fun getBalance(uuid: UUID): CompletableFuture<Double> {
        val future = CompletableFuture<Double>()
        loggedTransaction {
            future.completeAsync {
                Account.select(Account.uuid, Account.credit).where(Account.uuid eq uuid.toString()).first()[Account.credit]
            }
        }

        return future
    }

    override fun depositCredit(uuid: UUID, credit: Double) {
        loggedTransaction {
            Account.update({ Account.uuid eq uuid.toString() }) {
                it[Account.credit] = Account.credit plus credit
            }
        }
    }
    override fun withdrawCredit(uuid: UUID, credit: Double) {
        loggedTransaction {
            Account.update({ Account.uuid eq uuid.toString() }) {
                it[Account.credit] = Account.credit minus credit
            }
        }
    }
    override fun setCredit(uuid: UUID, credit: Double) {
        loggedTransaction {
            Account.update({ Account.uuid eq uuid.toString() }) {
                it[Account.credit] = credit
            }
        }
    }

    override fun purgeAccounts() {
        loggedTransaction { Account.deleteAll() }
    }

    override fun purgeDefaultAccounts(defaultCredit: Double) {
        loggedTransaction { Account.deleteWhere { credit eq defaultCredit } }
    }

    override fun purgeInvalidAccounts() {
        val validPlayerUUIDs = Bukkit.getOfflinePlayers().mapNotNull { runCatching { it.uniqueId }.getOrNull() }.map { it.toString() }
        loggedTransaction {
            Account.deleteWhere {
                uuid notInList validPlayerUUIDs
            }
        }
    }
}