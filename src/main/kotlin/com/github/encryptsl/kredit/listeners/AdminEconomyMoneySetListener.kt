package com.github.encryptsl.kredit.listeners

import com.github.encryptsl.kredit.api.events.AdminKreditSetEvent
import com.github.encryptsl.kredit.api.objects.ModernText
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class AdminEconomyMoneySetListener(private val creditLite: com.github.encryptsl.kredit.CreditLite) : Listener {

    @EventHandler
    fun onAdminEconomyMoneySet(event: AdminKreditSetEvent) {
        val sender: CommandSender = event.commandSender
        val target: OfflinePlayer = event.offlinePlayer
        val money: Double = event.money

        if (!creditLite.api.hasAccount(target)) {
            sender.sendMessage(
                ModernText.miniModernText(creditLite.locale.getMessage("messages.error.account_not_exist"),
                TagResolver.resolver(Placeholder.parsed("account", target.name.toString()))))
            return
        }

        creditLite.countTransactions["transactions"] = creditLite.countTransactions.getOrDefault("transactions", 0) + 1

        creditLite.api.setMoney(target, money)
        if (sender.name == target.name) {
            sender.sendMessage(
                ModernText.miniModernText(
                    creditLite.locale.getMessage("messages.self.set_credit"), TagResolver.resolver(Placeholder.parsed("credit", creditLite.api.fullFormatting(money)))))
            return
        }

        sender.sendMessage(
            ModernText.miniModernText(
            creditLite.locale.getMessage("messages.sender.set_credit"),
            TagResolver.resolver(Placeholder.parsed("target", target.name.toString()), Placeholder.parsed("credit", creditLite.api.fullFormatting(money)))))

        if (target.isOnline && creditLite.config.getBoolean("messages.target.notify_set")) {
            target.player?.sendMessage(
                ModernText.miniModernText(creditLite.locale.getMessage("messages.target.set_credit"),
                TagResolver.resolver(
                    Placeholder.parsed("sender", sender.name),
                    Placeholder.parsed("credit", creditLite.api.fullFormatting(money))
                )
            ))
        }
    }

}