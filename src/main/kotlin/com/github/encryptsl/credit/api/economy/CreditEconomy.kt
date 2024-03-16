package com.github.encryptsl.credit.api.economy

import com.github.encryptsl.credit.api.PlayerWalletCache
import com.github.encryptsl.credit.api.interfaces.CreditAPI
import com.github.encryptsl.credit.common.database.models.CreditModel
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.util.*

object CreditEconomy : CreditAPI {

    private val walletCache: PlayerWalletCache by lazy { PlayerWalletCache() }
    private val creditModel: CreditModel by lazy { CreditModel() }
    override fun createAccount(player: OfflinePlayer, startAmount: Double): Boolean {
        if (hasAccount(player)) return false

        creditModel.createPlayerAccount(player.name.toString(), player.uniqueId, startAmount)
        return true
    }

    override fun cacheAccount(player: OfflinePlayer, amount: Double): Boolean {
        if (!hasAccount(player)) return false

        walletCache.cacheAccount(player.uniqueId, amount)
        return true
    }

    override fun deleteAccount(player: OfflinePlayer): Boolean {
        if (!hasAccount(player)) return false

        return if (walletCache.isPlayerOnline(player.uniqueId)) {
            walletCache.removeAccount(player.uniqueId)
            true
        } else {
            creditModel.deletePlayerAccount(player.uniqueId)
            true
        }
    }

    override fun hasAccount(player: OfflinePlayer): Boolean {
        return creditModel.getExistPlayerAccount(player.uniqueId)
    }

    override fun has(player: OfflinePlayer, amount: Double): Boolean {
        return amount <= getBalance(player)
    }

    override fun getBalance(player: OfflinePlayer): Double {
        return if (walletCache.isPlayerOnline(player.uniqueId) || walletCache.isAccountCached(player.uniqueId))
            walletCache.getBalance(player.uniqueId)
        else
            creditModel.getBalance(player.uniqueId)
    }

    override fun deposit(player: OfflinePlayer, amount: Double) {
        if (walletCache.isPlayerOnline(player.uniqueId)) {
            cacheAccount(player, getBalance(player).plus(amount))
        } else {
            creditModel.depositCredit(player.uniqueId, amount)
        }
    }

    override fun withdraw(player: OfflinePlayer, amount: Double) {
        if (walletCache.isPlayerOnline(player.uniqueId)) {
            cacheAccount(player, getBalance(player).minus(amount))
        } else {
            creditModel.withdrawCredit(player.uniqueId, amount)
        }
    }

    override fun set(player: OfflinePlayer, amount: Double) {
        if (walletCache.isPlayerOnline(player.uniqueId)) {
            cacheAccount(player, amount)
        } else {
            creditModel.setCredit(player.uniqueId, amount)
        }
    }

    override fun syncAccount(offlinePlayer: OfflinePlayer) {
        walletCache.syncAccount(offlinePlayer.uniqueId)
    }

    override fun syncAccounts() {
        walletCache.syncAccounts()
    }

    override fun getTopBalance(): MutableMap<String, Double> {
        val databaseStoredData = creditModel.getTopBalance().filterNot { e -> Bukkit.getOfflinePlayer(e.key).name == null }

        return databaseStoredData.mapValues { e -> getBalance(Bukkit.getOfflinePlayer(UUID.fromString(e.key))) }.toMutableMap()
    }
}