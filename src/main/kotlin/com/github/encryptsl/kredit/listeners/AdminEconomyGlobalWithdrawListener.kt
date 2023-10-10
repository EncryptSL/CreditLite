package com.github.encryptsl.kredit.listeners

import com.github.encryptsl.kredit.api.events.AdminGlobalWithdrawEvent
import com.github.encryptsl.kredit.api.objects.ModernText
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class AdminEconomyGlobalWithdrawListener(private val creditLite: com.github.encryptsl.kredit.CreditLite) : Listener {
    @EventHandler
    fun onAdminEconomyGlobalWithdraw(event: AdminGlobalWithdrawEvent) {
        val sender: CommandSender = event.commandSender
        val money = event.money
        val offlinePlayers = Bukkit.getOfflinePlayers()

        offlinePlayers.filter { p -> creditLite.api.hasAccount(p) }.forEach { a ->
            creditLite.api.withDrawMoney(a, money)
        }

        creditLite.countTransactions["transactions"] = creditLite.countTransactions.getOrDefault("transactions", 0) + 1

        sender.sendMessage(
            ModernText.miniModernText(creditLite.locale.getMessage("messages.global.withdraw_credit"),
            TagResolver.resolver(
                Placeholder.parsed("money", creditLite.api.fullFormatting(money))
            )
        ))
        if (creditLite.config.getBoolean("messages.global.notify_withdraw"))
            Bukkit.broadcast(
                ModernText.miniModernText(creditLite.locale.getMessage("messages.broadcast.withdraw_credit"),
                TagResolver.resolver(
                    Placeholder.parsed("sender", sender.name),
                    Placeholder.parsed("credit", creditLite.api.fullFormatting(money))
                )
            ))
        return
    }
}