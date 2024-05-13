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
        if (hasAccount(player.uniqueId)) return false

        creditModel.createPlayerAccount(player.name.toString(), player.uniqueId, startAmount)
        return true
    }

    override fun cacheAccount(uuid: UUID, amount: Double): Boolean {
        if (!hasAccount(uuid)) return false

        walletCache.cacheAccount(uuid, amount)
        return true
    }

    override fun deleteAccount(uuid: UUID): Boolean {
        if (!hasAccount(uuid)) return false

        return if (walletCache.isPlayerOnline(uuid)) {
            walletCache.removeAccount(uuid)
            true
        } else {
            creditModel.deletePlayerAccount(uuid)
            true
        }
    }

    override fun hasAccount(uuid: UUID): Boolean {
        return creditModel.getExistPlayerAccount(uuid).join()
    }

    override fun has(uuid: UUID, amount: Double): Boolean {
        return amount <= getBalance(uuid)
    }

    override fun getBalance(uuid: UUID): Double {
        return if (walletCache.isPlayerOnline(uuid) || walletCache.isAccountCached(uuid))
            walletCache.getBalance(uuid)
        else
            creditModel.getBalance(uuid).join()
    }

    override fun deposit(uuid: UUID, amount: Double) {
        if (walletCache.isPlayerOnline(uuid)) {
            cacheAccount(uuid, getBalance(uuid).plus(amount))
        } else {
            creditModel.depositCredit(uuid, amount)
        }
    }

    override fun withdraw(uuid: UUID, amount: Double) {
        if (walletCache.isPlayerOnline(uuid)) {
            cacheAccount(uuid, getBalance(uuid).minus(amount))
        } else {
            creditModel.withdrawCredit(uuid, amount)
        }
    }

    override fun set(uuid: UUID, amount: Double) {
        if (walletCache.isPlayerOnline(uuid)) {
            cacheAccount(uuid, amount)
        } else {
            creditModel.setCredit(uuid, amount)
        }
    }

    override fun syncAccount(uuid: UUID) {
        walletCache.syncAccount(uuid)
    }

    override fun syncAccounts() {
        walletCache.syncAccounts()
    }

    override fun getTopBalance(): Map<String, Double> {
        val databaseStoredData = creditModel.getTopBalance().filterNot { e -> Bukkit.getOfflinePlayer(e.key).name == null }

        return databaseStoredData
            .mapValues { e -> getBalance(UUID.fromString(e.key)) }
            .toList()
            .sortedByDescending { (_,e) -> e }
            .toMap()
    }
}