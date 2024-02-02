package com.github.encryptsl.credit.listeners.admin

import com.github.encryptsl.credit.api.economy.CreditEconomy
import com.github.encryptsl.credit.api.events.GlobalCreditSetEvent
import com.github.encryptsl.credit.api.objects.ModernText
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class GlobalCreditSetListener(private val creditLite: com.github.encryptsl.credit.CreditLite) : Listener {
    @EventHandler
    fun onAdminEconomyGlobalSet(event: GlobalCreditSetEvent) {
        val sender: CommandSender = event.commandSender
        val money = event.money
        val offlinePlayers = Bukkit.getOfflinePlayers()

        offlinePlayers.filter { a -> CreditEconomy.hasAccount(a) }.forEach { offlinePlayer ->
            CreditEconomy.set(offlinePlayer, money)
        }

        creditLite.countTransactions["transactions"] = creditLite.countTransactions.getOrDefault("transactions", 0) + offlinePlayers.size

        sender.sendMessage(
            ModernText.miniModernText(creditLite.locale.getMessage("messages.global.set_credit"),
            TagResolver.resolver(
                Placeholder.parsed("money", creditLite.creditEconomyFormatting.fullFormatting(money))
            )
        ))
        if (creditLite.config.getBoolean("messages.global.notify_set"))
            Bukkit.broadcast(
                ModernText.miniModernText(creditLite.locale.getMessage("messages.broadcast.set_credit"),
                    TagResolver.resolver(
                        Placeholder.parsed("sender", sender.name),
                        Placeholder.parsed("credit", creditLite.creditEconomyFormatting.fullFormatting(money))
                    )))
            return
    }
}