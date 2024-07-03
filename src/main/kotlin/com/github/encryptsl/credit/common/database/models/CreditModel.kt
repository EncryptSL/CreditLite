package com.github.encryptsl.credit.common.database.models

import com.github.encryptsl.credit.api.interfaces.CreditDataSourceSQL
import com.github.encryptsl.credit.common.database.entity.User
import com.github.encryptsl.credit.common.database.tables.Account
import com.github.encryptsl.credit.common.extensions.loggedTransaction
import org.bukkit.Bukkit
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.CompletableFuture

class CreditModel : CreditDataSourceSQL {

    override fun createPlayerAccount(username: String, uuid: UUID, credit: BigDecimal) {
        loggedTransaction {
            Account.insertIgnore {
                it[Account.username] = username
                it[Account.uuid] = uuid
                it[Account.credit] = credit
            }
        }
    }

    override fun deletePlayerAccount(uuid: UUID) {
        loggedTransaction {
            Account.deleteWhere { Account.uuid eq uuid }
        }
    }

    override fun getUserByUUID(uuid: UUID): CompletableFuture<User> {
        val future = CompletableFuture<User>()
        loggedTransaction {
            val row = Account.select(Account.uuid, Account.username, Account.credit).where(Account.uuid eq uuid).singleOrNull()
            if (row == null) {
                future.completeExceptionally(RuntimeException("User not found !"))
            } else {
                future.completeAsync { User(row[Account.username], row[Account.uuid], row[Account.credit]) }
            }
        }

        return future
    }

    override fun getExistPlayerAccount(uuid: UUID): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()

        val boolean = loggedTransaction {
            !Account.select(Account.uuid).where(Account.uuid eq uuid).empty()
        }
        future.completeAsync { boolean }
        return future
    }

    override fun getTopBalance(): MutableMap<String, BigDecimal> = loggedTransaction {
        Account.selectAll().orderBy(Account.credit, SortOrder.DESC).associate {
            it[Account.username] to it[Account.credit]
        }.toMutableMap()
    }

    override fun depositCredit(uuid: UUID, credit: BigDecimal) {
        loggedTransaction {
            Account.update({ Account.uuid eq uuid }) {
                it[Account.credit] = Account.credit plus credit
            }
        }
    }
    override fun withdrawCredit(uuid: UUID, credit: BigDecimal) {
        loggedTransaction {
            Account.update({ Account.uuid eq uuid }) {
                it[Account.credit] = Account.credit minus credit
            }
        }
    }
    override fun setCredit(uuid: UUID, credit: BigDecimal) {
        loggedTransaction {
            Account.update({ Account.uuid eq uuid }) {
                it[Account.credit] = credit
            }
        }
    }

    override fun purgeAccounts() {
        loggedTransaction { Account.deleteAll() }
    }

    override fun purgeDefaultAccounts(defaultCredit: BigDecimal) {
        loggedTransaction { Account.deleteWhere { credit eq defaultCredit } }
    }

    override fun purgeInvalidAccounts() {
        val validPlayerUUIDs = Bukkit.getOfflinePlayers().mapNotNull { runCatching { it.uniqueId }.getOrNull() }.map { it }
        loggedTransaction {
            Account.deleteWhere {
                uuid notInList validPlayerUUIDs
            }
        }
    }
}