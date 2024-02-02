package com.github.encryptsl.credit.listeners.admin

import com.github.encryptsl.credit.api.economy.CreditEconomy
import com.github.encryptsl.credit.api.events.CreditSetEvent
import com.github.encryptsl.credit.api.objects.ModernText
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class CreditSetListener(private val creditLite: com.github.encryptsl.credit.CreditLite) : Listener {

    @EventHandler
    fun onAdminEconomyMoneySet(event: CreditSetEvent) {
        val sender: CommandSender = event.commandSender
        val target: OfflinePlayer = event.offlinePlayer
        val money: Double = event.money

        if (!CreditEconomy.hasAccount(target)) {
            sender.sendMessage(
                ModernText.miniModernText(creditLite.locale.getMessage("messages.error.account_not_exist"),
                TagResolver.resolver(Placeholder.parsed("account", target.name.toString()))))
            return
        }

        creditLite.countTransactions["transactions"] = creditLite.countTransactions.getOrDefault("transactions", 0) + 1

        CreditEconomy.set(target, money)
        if (sender.name == target.name) {
            sender.sendMessage(
                ModernText.miniModernText(
                    creditLite.locale.getMessage("messages.self.set_credit"), TagResolver.resolver(Placeholder.parsed("credit", creditLite.creditEconomyFormatting.fullFormatting(money)))))
            return
        }

        sender.sendMessage(
            ModernText.miniModernText(
            creditLite.locale.getMessage("messages.sender.set_credit"),
            TagResolver.resolver(Placeholder.parsed("target", target.name.toString()), Placeholder.parsed("credit", creditLite.creditEconomyFormatting.fullFormatting(money)))))

        if (target.isOnline && creditLite.config.getBoolean("messages.target.notify_set")) {
            target.player?.sendMessage(
                ModernText.miniModernText(creditLite.locale.getMessage("messages.target.set_credit"),
                TagResolver.resolver(
                    Placeholder.parsed("sender", sender.name),
                    Placeholder.parsed("credit", creditLite.creditEconomyFormatting.fullFormatting(money))
                )
            ))
        }
    }

}