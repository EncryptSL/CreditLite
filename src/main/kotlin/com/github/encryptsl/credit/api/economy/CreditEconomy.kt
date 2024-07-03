package com.github.encryptsl.credit.api.economy

import com.github.encryptsl.credit.api.PlayerWalletCache
import com.github.encryptsl.credit.api.interfaces.CreditAPI
import com.github.encryptsl.credit.common.database.entity.User
import com.github.encryptsl.credit.common.database.models.CreditModel
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.CompletableFuture

object CreditEconomy : CreditAPI {

    private val walletCache: PlayerWalletCache by lazy { PlayerWalletCache() }
    private val creditModel: CreditModel by lazy { CreditModel() }

    override fun createAccount(player: OfflinePlayer, startAmount: BigDecimal): Boolean {
        return getUserByUUID(player).thenApply { false }.exceptionally {
            creditModel.createPlayerAccount(player.name.toString(), player.uniqueId, startAmount)
            return@exceptionally true
        }.get()
    }

    override fun cacheAccount(player: OfflinePlayer, amount: BigDecimal) {
        getUserByUUID(player).thenAccept { walletCache.cacheAccount(player.uniqueId, amount) }
    }

    override fun deleteAccount(player: OfflinePlayer): Boolean {
        return getUserByUUID(player).thenApply {
            walletCache.removeAccount(player.uniqueId)
            creditModel.deletePlayerAccount(player.uniqueId)
            return@thenApply true
        }.exceptionally {
            return@exceptionally false
        }.join()
    }

    override fun hasAccount(uuid: UUID): CompletableFuture<Boolean> {
        return creditModel.getExistPlayerAccount(uuid)
    }

    override fun has(player: OfflinePlayer, amount: BigDecimal): Boolean {
        return amount <= getBalance(player)
    }

    override fun getUserByUUID(player: OfflinePlayer): CompletableFuture<User> {
        val future = CompletableFuture<User>()

        if (walletCache.isPlayerOnline(player.uniqueId) || walletCache.isAccountCached(player.uniqueId)) {
            future.completeAsync { User(player.name.toString(), player.uniqueId, walletCache.getBalance(player.uniqueId)) }
        } else {
            return creditModel.getUserByUUID(player.uniqueId)
        }

        return future
    }

    override fun getBalance(player: OfflinePlayer): BigDecimal {
        return getUserByUUID(player).join().money
    }

    override fun deposit(player: OfflinePlayer, amount: BigDecimal) {
        if (walletCache.isPlayerOnline(player.uniqueId)) {
            cacheAccount(player, getBalance(player).plus(amount))
        } else {
            creditModel.depositCredit(player.uniqueId, amount)
        }
    }

    override fun withdraw(player: OfflinePlayer, amount: BigDecimal) {
        if (walletCache.isPlayerOnline(player.uniqueId)) {
            cacheAccount(player, getBalance(player).minus(amount))
        } else {
            creditModel.withdrawCredit(player.uniqueId, amount)
        }
    }

    override fun set(player: OfflinePlayer, amount: BigDecimal) {
        if (walletCache.isPlayerOnline(player.uniqueId)) {
            cacheAccount(player, amount)
        } else {
            creditModel.setCredit(player.uniqueId, amount)
        }
    }

    override fun syncAccount(player: OfflinePlayer) {
        walletCache.syncAccount(player.uniqueId)
    }

    override fun syncAccounts() {
        walletCache.syncAccounts()
    }

    override fun getTopBalance(): Map<String, BigDecimal> {
        val databaseStoredData = creditModel.getTopBalance().filterNot { e -> Bukkit.getOfflinePlayer(e.key).name == null }

        return databaseStoredData
            .mapValues { e -> getBalance(Bukkit.getOfflinePlayer(UUID.fromString(e.key))) }
            .toList()
            .sortedByDescending { (_,e) -> e }
            .toMap()
    }
}