package com.github.encryptsl.credit.listeners

import com.github.encryptsl.credit.api.enums.OperationType
import com.github.encryptsl.credit.api.events.AccountManageEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener(private val creditLite: com.github.encryptsl.credit.CreditLite) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player: Player = event.player
        creditLite.pluginManager.callEvent(AccountManageEvent(player, OperationType.CREATE_ACCOUNT))
        creditLite.pluginManager.callEvent(AccountManageEvent(player, OperationType.CACHING_ACCOUNT))
    }
}