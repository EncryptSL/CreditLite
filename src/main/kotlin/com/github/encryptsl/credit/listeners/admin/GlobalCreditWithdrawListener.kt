package com.github.encryptsl.credit.listeners.admin

import com.github.encryptsl.credit.api.economy.CreditEconomy
import com.github.encryptsl.credit.api.events.GlobalCreditWithdrawEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class GlobalCreditWithdrawListener(private val creditLite: com.github.encryptsl.credit.CreditLite) : Listener {
    @EventHandler
    fun onGlobalCreditWithdraw(event: GlobalCreditWithdrawEvent) {
        val sender: CommandSender = event.commandSender
        val money = event.money
        val offlinePlayers = Bukkit.getOfflinePlayers()

        for (player in offlinePlayers) {
            if (!CreditEconomy.hasAccount(player)) continue

            CreditEconomy.withdraw(player, money)
        }

        sender.sendMessage(creditLite.locale.translation("messages.global.withdraw_credit",
            Placeholder.parsed("money", creditLite.creditEconomyFormatting.fullFormatting(money))
        ))
        if (creditLite.config.getBoolean("messages.global.notify_withdraw"))
            Bukkit.broadcast(creditLite.locale.translation("messages.broadcast.withdraw_credit",
                TagResolver.resolver(
                    Placeholder.parsed("sender", sender.name),
                    Placeholder.parsed("credit", creditLite.creditEconomyFormatting.fullFormatting(money))
                )
            ))
        return
    }
}