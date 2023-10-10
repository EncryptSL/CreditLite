package com.github.encryptsl.credit.listeners

import com.github.encryptsl.credit.api.events.AdminKreditWithdrawEvent
import com.github.encryptsl.credit.api.objects.ModernText
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class AdminEconomyMoneyWithdrawListener(private val creditLite: com.github.encryptsl.credit.CreditLite) : Listener {

    @EventHandler
    fun onAdminEconomyMoneyWithdraw(event: AdminKreditWithdrawEvent) {
        val sender: CommandSender = event.commandSender
        val target: OfflinePlayer = event.offlinePlayer
        val money: Double = event.money

        if (!creditLite.api.hasAccount(target)) {
            sender.sendMessage(
                ModernText.miniModernText(creditLite.locale.getMessage("messages.error.account_not_exist"),
                TagResolver.resolver(Placeholder.parsed("account", target.name.toString()))))
            return
        }

        if (!creditLite.api.has(target, money)) {
            sender.sendMessage(ModernText.miniModernText(creditLite.locale.getMessage("messages.error.insufficient_funds")))
            return
        }

        creditLite.countTransactions["transactions"] = creditLite.countTransactions.getOrDefault("transactions", 0) + 1
        creditLite.api.withDrawMoney(target, money)
        if (sender.name == target.name) {
            sender.sendMessage(
                ModernText.miniModernText(
                    creditLite.locale.getMessage("messages.self.withdraw_credit"),
                    TagResolver.resolver(Placeholder.parsed("credit", creditLite.api.fullFormatting(money)))
                )
            )
            return
        }

        sender.sendMessage(
            ModernText.miniModernText(
                creditLite.locale.getMessage("messages.sender.withdraw_credit"),
                TagResolver.resolver(Placeholder.parsed("target", target.name.toString()), Placeholder.parsed("credit", creditLite.api.fullFormatting(money)))))
        if (target.isOnline && creditLite.config.getBoolean("messages.target.notify_withdraw")) {
            target.player?.sendMessage(
                ModernText.miniModernText(creditLite.locale.getMessage("messages.target.withdraw_credit"),
                TagResolver.resolver(
                    Placeholder.parsed("sender", sender.name),
                    Placeholder.parsed("credit", creditLite.api.fullFormatting(money))
                )
            ))
        }
    }

}