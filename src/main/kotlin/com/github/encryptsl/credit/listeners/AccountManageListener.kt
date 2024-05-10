package com.github.encryptsl.credit.listeners

import com.github.encryptsl.credit.api.economy.CreditEconomy
import com.github.encryptsl.credit.api.enums.OperationType
import com.github.encryptsl.credit.api.events.AccountManageEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class AccountManageListener(private val creditLite: com.github.encryptsl.credit.CreditLite) : Listener {

    @EventHandler
    fun onEconomyManage(event: AccountManageEvent) {
        val player: Player = event.player

        when (event.operationType) {
            OperationType.CREATE_ACCOUNT -> CreditEconomy.createAccount(player, creditLite.config.getDouble("economy.starting_balance"))
            OperationType.CACHING_ACCOUNT -> CreditEconomy.cacheAccount(player.uniqueId, creditLite.creditModel.getBalance(player.uniqueId))
            OperationType.SYNC_ACCOUNT -> CreditEconomy.syncAccount(player.uniqueId)
            OperationType.REMOVE_ACCOUNT -> CreditEconomy.deleteAccount(player.uniqueId)
        }
    }
}