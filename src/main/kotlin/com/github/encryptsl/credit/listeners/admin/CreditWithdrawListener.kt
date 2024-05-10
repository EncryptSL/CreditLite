package com.github.encryptsl.credit.listeners.admin

import com.github.encryptsl.credit.api.economy.CreditEconomy
import com.github.encryptsl.credit.api.events.CreditWithdrawEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class CreditWithdrawListener(private val creditLite: com.github.encryptsl.credit.CreditLite) : Listener {

    @EventHandler
    fun onAdminEconomyMoneyWithdraw(event: CreditWithdrawEvent) {
        val sender: CommandSender = event.commandSender
        val target: OfflinePlayer = event.offlinePlayer
        val money: Double = event.money

        if (!CreditEconomy.hasAccount(target.uniqueId))
            return sender.sendMessage(
                creditLite.locale.translation("messages.error.account_not_exist",
                    Placeholder.parsed("account", target.name.toString())
                ))

        if (!CreditEconomy.has(target.uniqueId, money))
            return sender.sendMessage(creditLite.locale.translation("messages.error.insufficient_funds"))

        if (sender.name == target.name)
            return sender.sendMessage(
                    creditLite.locale.translation("messages.self.withdraw_credit",
                    Placeholder.parsed("credit", creditLite.creditEconomyFormatting.fullFormatting(money))
                )
            )

        CreditEconomy.withdraw(target.uniqueId, money)
        creditLite.monologModel.info(creditLite.locale.getMessage("messages.monolog.admin.normal.withdraw")
            .replace("<sender>", sender.name)
            .replace("<target>", target.name.toString())
            .replace("<credit>", creditLite.creditEconomyFormatting.fullFormatting(money))
        )

        sender.sendMessage(creditLite.locale.translation("messages.sender.withdraw_credit", TagResolver.resolver(
                    Placeholder.parsed("target", target.name.toString()),
                    Placeholder.parsed("credit", creditLite.creditEconomyFormatting.fullFormatting(money))
        )))
        if (target.isOnline && creditLite.config.getBoolean("messages.target.notify_withdraw")) {
            target.player?.sendMessage(
                creditLite.locale.translation("messages.target.withdraw_credit", TagResolver.resolver(
                    Placeholder.parsed("sender", sender.name),
                    Placeholder.parsed("credit", creditLite.creditEconomyFormatting.fullFormatting(money))
                )
            ))
        }
    }

}