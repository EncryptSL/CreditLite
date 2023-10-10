package com.github.encryptsl.credit.listeners

import com.github.encryptsl.credit.api.events.AdminGlobalDepositEvent
import com.github.encryptsl.credit.api.objects.ModernText
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class AdminEconomyGlobalDepositListener(private val creditLite: com.github.encryptsl.credit.CreditLite) : Listener {

    @EventHandler
    fun onAdminEconomyGlobalDeposit(event: AdminGlobalDepositEvent) {
        val sender: CommandSender = event.commandSender
        val money = event.money
        val offlinePlayers = Bukkit.getOfflinePlayers()

        offlinePlayers.filter { p -> creditLite.api.hasAccount(p) }.forEach { a ->
            creditLite.api.depositMoney(a, money)
        }

        creditLite.countTransactions["transactions"] = creditLite.countTransactions.getOrDefault("transactions", 0) + offlinePlayers.size

        sender.sendMessage(
            ModernText.miniModernText(creditLite.locale.getMessage("messages.global.add_credit"),
            TagResolver.resolver(
                Placeholder.parsed("credit", creditLite.api.fullFormatting(money))
            )
        ))
        if (!creditLite.config.getBoolean("messages.global.notify_add")) {
            Bukkit.broadcast(
                ModernText.miniModernText(creditLite.locale.getMessage("messages.broadcast.add_credit"),
                TagResolver.resolver(
                    Placeholder.parsed("sender", sender.name),
                    Placeholder.parsed("credit", creditLite.api.fullFormatting(money))
                )
            ))
        }
    }

}