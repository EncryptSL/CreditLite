package com.github.encryptsl.credit.listeners

import com.github.encryptsl.credit.CreditLite
import com.github.encryptsl.credit.api.economy.CreditEconomy
import com.github.encryptsl.credit.api.enums.OperationType
import com.github.encryptsl.credit.api.events.AccountManageEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class AccountManageListener(
    private val creditLite: CreditLite
) : Listener {

    @EventHandler
    fun onEconomyManage(event: AccountManageEvent) {
        val player: Player = event.player
        when (event.operationType) {
            OperationType.CREATE_ACCOUNT -> CreditEconomy.createAccount(player, creditLite.config.getDouble("economy.starting_balance").toBigDecimal())
            OperationType.CACHING_ACCOUNT -> {
                creditLite.creditModel.getUserByUUID(player.uniqueId).thenAccept {
                    CreditEconomy.cacheAccount(player, it.money)
                }
            }
            OperationType.SYNC_ACCOUNT -> CreditEconomy.syncAccount(player)
            OperationType.REMOVE_ACCOUNT -> CreditEconomy.deleteAccount(player)
        }
    }
}