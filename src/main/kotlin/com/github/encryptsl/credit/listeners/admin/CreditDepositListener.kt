package com.github.encryptsl.credit.listeners.admin

import com.github.encryptsl.credit.api.economy.CreditEconomy
import com.github.encryptsl.credit.api.events.CreditDepositEvent
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

        if (sender.name == target.name || !sender.hasPermission("credit.admin.add.self.exempt"))
            return sender.sendMessage(creditLite.locale.translation("messages.message.self.add_credit",
                Placeholder.parsed("credit", creditLite.creditEconomyFormatting.fullFormatting(money))
            ))

        CreditEconomy.getUserByUUID(target).thenApply {
            CreditEconomy.deposit(target, money)
            creditLite.monologModel.info(creditLite.locale.getMessage("messages.monolog.admin.normal.deposit")
                .replace("<sender>", sender.name)
                .replace("<target>", target.name.toString())
                .replace("<credit>", creditLite.creditEconomyFormatting.fullFormatting(money))
            )
        }.exceptionally {
            sender.sendMessage(creditLite.locale.translation("messages.error.account_not_exist",
                Placeholder.parsed("account", target.name.toString())))
        }

        sender.sendMessage(
            creditLite.locale.translation("messages.sender.add_credit", TagResolver.resolver(
                Placeholder.parsed("target", target.name.toString()),
                Placeholder.parsed("credit", creditLite.creditEconomyFormatting.fullFormatting(money))
            ))
        )
        if (target.isOnline && creditLite.config.getBoolean("messages.target.notify_add")) {
            if (silent) {
                target.player?.sendMessage(
                    creditLite.locale.translation("messages.target.add_credit_silent",
                    Placeholder.parsed("credit", creditLite.creditEconomyFormatting.fullFormatting(money))
                ))
                return
            }

            target.player?.sendMessage(
                creditLite.locale.translation("messages.target.add_credit",
                TagResolver.resolver(
                    Placeholder.parsed("sender", sender.name),
                    Placeholder.parsed("credit", creditLite.creditEconomyFormatting.fullFormatting(money))
                )
            ))
        }
    }

}