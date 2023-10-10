package com.github.encryptsl.credit.listeners

import com.github.encryptsl.credit.api.enums.OperationType
import com.github.encryptsl.credit.api.events.AccountManageEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener(private val creditLite: com.github.encryptsl.credit.CreditLite) : Listener {

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        creditLite.pluginManager.callEvent(AccountManageEvent(player, OperationType.SYNC_ACCOUNT))
    }

}