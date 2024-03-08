package com.github.encryptsl.credit.listeners.admin

import com.github.encryptsl.credit.api.economy.CreditEconomy
import com.github.encryptsl.credit.api.events.CreditDepositEvent
import com.github.encryptsl.credit.api.objects.ModernText
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class CreditDepositListener(private val creditLite: com.github.encryptsl.credit.CreditLite) : Listener {

    @EventHandler
    fun onCreditDeposit(event: CreditDepositEvent) {
        val sender: CommandSender = event.commandSender
        val target: OfflinePlayer = event.offlinePlayer
        val money: Double = event.money
        val silent: Boolean = event.silent

        if (!CreditEconomy.hasAccount(target))
            return sender.sendMessage(ModernText.miniModernText(creditLite.locale.getMessage("messages.error.account_not_exist"),
                TagResolver.resolver(Placeholder.parsed("account", target.name.toString()))))

        if (sender.name == target.name && !target.isOp)
            return sender.sendMessage(
                ModernText.miniModernText(creditLite.locale.getMessage("messages.error.self_pay"), TagResolver.resolver(Placeholder.parsed("credit", creditLite.creditEconomyFormatting.fullFormatting(money)))))

        CreditEconomy.deposit(target, money)

        sender.sendMessage(
            ModernText.miniModernText(
            creditLite.locale.getMessage("messages.sender.add_credit"),
            TagResolver.resolver(Placeholder.parsed("target", target.name.toString()), Placeholder.parsed("credit", creditLite.creditEconomyFormatting.fullFormatting(money)))
        ))
        if (target.isOnline && creditLite.config.getBoolean("messages.target.notify_add")) {
            if (silent) {
                target.player?.sendMessage(ModernText.miniModernText(
                    creditLite.locale.getMessage("messages.target.add_credit_silent"),
                    Placeholder.parsed("credit", creditLite.creditEconomyFormatting.fullFormatting(money))
                ))
                return
            }

            target.player?.sendMessage(
                ModernText.miniModernText(creditLite.locale.getMessage("messages.target.add_credit"),
                TagResolver.resolver(
                    Placeholder.parsed("sender", sender.name),
                    Placeholder.parsed("credit", creditLite.creditEconomyFormatting.fullFormatting(money))
                )
            ))
        }
    }

}