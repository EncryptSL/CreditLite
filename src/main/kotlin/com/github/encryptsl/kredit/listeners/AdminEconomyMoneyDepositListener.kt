package com.github.encryptsl.kredit.listeners

import com.github.encryptsl.kredit.api.events.AdminKreditDepositEvent
import com.github.encryptsl.kredit.api.objects.ModernText
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class AdminEconomyMoneyDepositListener(private val creditLite: com.github.encryptsl.kredit.CreditLite) : Listener {

    @EventHandler
    fun onAdminEconomyDeposit(event: AdminKreditDepositEvent) {
        val sender: CommandSender = event.commandSender
        val target: OfflinePlayer = event.offlinePlayer
        val money: Double = event.money

        if (!creditLite.api.hasAccount(target)) {
            sender.sendMessage(ModernText.miniModernText(creditLite.locale.getMessage("messages.error.account_not_exist"),
                TagResolver.resolver(Placeholder.parsed("account", target.name.toString()))))
            return
        }

        creditLite.countTransactions["transactions"] = creditLite.countTransactions.getOrDefault("transactions", 0) + 1

        creditLite.api.depositMoney(target, money)
        if (sender.name == target.name && !target.isOp) {
            sender.sendMessage(
                ModernText.miniModernText(creditLite.locale.getMessage("messages.error.self_pay"), TagResolver.resolver(Placeholder.parsed("money", creditLite.api.fullFormatting(money)))))
            return
        }

        sender.sendMessage(
            ModernText.miniModernText(
            creditLite.locale.getMessage("messages.sender.add_money"),
            TagResolver.resolver(Placeholder.parsed("target", target.name.toString()), Placeholder.parsed("credit", creditLite.api.fullFormatting(money)))
        ))
        if (target.isOnline && creditLite.config.getBoolean("messages.target.notify_add")) {
            target.player?.sendMessage(
                ModernText.miniModernText(creditLite.locale.getMessage("messages.target.add_money"),
                TagResolver.resolver(
                    Placeholder.parsed("sender", sender.name),
                    Placeholder.parsed("money", creditLite.api.fullFormatting(money))
                )
            ))
        }
    }

}