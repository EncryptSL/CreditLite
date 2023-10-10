package com.github.encryptsl.kredit.listeners

import com.github.encryptsl.kredit.api.enums.OperationType
import com.github.encryptsl.kredit.api.events.AccountManageEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener(private val creditLite: com.github.encryptsl.kredit.CreditLite) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player: Player = event.player
        creditLite.pluginManager.callEvent(AccountManageEvent(player, OperationType.CREATE_ACCOUNT))
        creditLite.pluginManager.callEvent(AccountManageEvent(player, OperationType.CACHING_ACCOUNT))
    }
}