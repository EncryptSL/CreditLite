package com.github.encryptsl.credit.listeners

import com.github.encryptsl.credit.CreditLite
import com.github.encryptsl.credit.api.enums.OperationType
import com.github.encryptsl.credit.api.events.AccountManageEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent

class PlayerLoginListener(private val creditLite: CreditLite) : Listener {

    @EventHandler
    fun onPlayerLogin(event: PlayerLoginEvent) {
        creditLite.pluginManager.callEvent(AccountManageEvent(event.player, OperationType.CREATE_ACCOUNT))
    }

}