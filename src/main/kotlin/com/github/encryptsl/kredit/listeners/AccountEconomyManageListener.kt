package com.github.encryptsl.kredit.listeners

import com.github.encryptsl.kredit.api.enums.OperationType
import com.github.encryptsl.kredit.api.events.AccountManageEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class AccountEconomyManageListener(private val creditLite: com.github.encryptsl.kredit.CreditLite) : Listener {

    @EventHandler
    fun onEconomyManage(event: AccountManageEvent) {
        val player: Player = event.player

        when (event.operationType) {
            OperationType.CREATE_ACCOUNT -> { creditLite.api.createAccount(player, creditLite.config.getDouble("economy.starting_balance")) }
            OperationType.CACHING_ACCOUNT -> { creditLite.api.cacheAccount(player, creditLite.creditModel.getBalance(player.uniqueId))}
            OperationType.SYNC_ACCOUNT -> creditLite.api.syncAccount(player)
            OperationType.REMOVE_ACCOUNT -> creditLite.api.deleteAccount(player)
        }
    }
}