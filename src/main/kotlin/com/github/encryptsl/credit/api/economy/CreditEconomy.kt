package com.github.encryptsl.credit.api.economy

import com.github.encryptsl.credit.api.PlayerAccount
import com.github.encryptsl.credit.api.interfaces.CreditAPI
import com.github.encryptsl.credit.database.models.CreditModel
import com.github.encryptsl.credit.extensions.compactFormat
import com.github.encryptsl.credit.extensions.moneyFormat
import org.bukkit.OfflinePlayer
import org.bukkit.plugin.Plugin

class CreditEconomy(val plugin: Plugin) : CreditAPI {

    private val creditModel: CreditModel by lazy { CreditModel() }
    private val playerAccount: PlayerAccount by lazy { PlayerAccount(plugin) }

    override fun createAccount(player: OfflinePlayer, startAmount: Double): Boolean {
        if (hasAccount(player)) return false

        creditModel.createPlayerAccount(player.uniqueId, startAmount)
        return true
    }

    override fun cacheAccount(player: OfflinePlayer, amount: Double): Boolean {
        if (!hasAccount(player)) return false

        playerAccount.cacheAccount(player.uniqueId, amount)
        return true
    }

    override fun deleteAccount(player: OfflinePlayer): Boolean {
        if (!hasAccount(player)) return false

        return if (playerAccount.isPlayerOnline(player.uniqueId)) {
            playerAccount.removeAccount(player.uniqueId)
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
        return if (playerAccount.isPlayerOnline(player.uniqueId) || playerAccount.isAccountCached(player.uniqueId))
            playerAccount.getBalance(player.uniqueId)
        else
            creditModel.getBalance(player.uniqueId)
    }

    override fun depositMoney(player: OfflinePlayer, amount: Double) {
        if (playerAccount.isPlayerOnline(player.uniqueId)) {
            cacheAccount(player, getBalance(player).plus(amount))
        } else {
            creditModel.depositCredit(player.uniqueId, amount)
        }
    }

    override fun withDrawMoney(player: OfflinePlayer, amount: Double) {
        if (playerAccount.isPlayerOnline(player.uniqueId)) {
            cacheAccount(player, getBalance(player).minus(amount))
        } else {
            creditModel.withdrawCredit(player.uniqueId, amount)
        }
    }

    override fun setMoney(player: OfflinePlayer, amount: Double) {
        if (playerAccount.isPlayerOnline(player.uniqueId)) {
            cacheAccount(player, amount)
        } else {
            creditModel.setCredit(player.uniqueId, amount)
        }
    }

    override fun syncAccount(offlinePlayer: OfflinePlayer) {
        playerAccount.syncAccount(offlinePlayer.uniqueId)
    }

    override fun syncAccounts() {
        playerAccount.syncAccounts()
    }

    override fun getTopBalance(): MutableMap<String, Double> {
        return creditModel.getTopBalance() // This must be same, because cache can be removed or cleared if player leave.
    }

    override fun compacted(amount: Double): String {
        return amount.compactFormat(plugin.config.getString("formatting.currency_pattern").toString(), plugin.config.getString("formatting.compact_pattern").toString(), plugin.config.getString("formatting.currency_locale").toString())
    }

    override fun formatted(amount: Double): String {
        return amount.moneyFormat(plugin.config.getString("formatting.currency_pattern").toString(), plugin.config.getString("formatting.currency_locale").toString())
    }

    override fun fullFormatting(amount: Double): String {
        val value = if (plugin.config.getBoolean("plugin.economy.compact_display")) {
            compacted(amount)
        }
        else {
            formatted(amount)
        }
        return "${plugin.config.getString("economy.currency_prefix").toString()}${value} ${plugin.config.getString("economy.currency_name").toString()}"
    }
}