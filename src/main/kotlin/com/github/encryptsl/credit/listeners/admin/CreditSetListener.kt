package com.github.encryptsl.credit.listeners.admin

import com.github.encryptsl.credit.api.economy.CreditEconomy
import com.github.encryptsl.credit.api.events.CreditSetEvent
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

        CreditEconomy.getUserByUUID(target).thenApply {
            CreditEconomy.set(target, money)
            creditLite.monologModel.info(creditLite.locale.getMessage("messages.monolog.admin.normal.set")
                .replace("<sender>", sender.name)
                .replace("<target>", target.name.toString())
                .replace("<credit>", creditLite.creditEconomyFormatting.fullFormatting(money))
            )
        }

        if (sender.name == target.name)
            return sender.sendMessage(
                creditLite.locale.translation("messages.self.set_credit",
                    Placeholder.parsed("credit", creditLite.creditEconomyFormatting.fullFormatting(money))
                ))

        sender.sendMessage(
            creditLite.locale.translation("messages.sender.set_credit", TagResolver.resolver(
                Placeholder.parsed("target", target.name.toString()),
                Placeholder.parsed("credit", creditLite.creditEconomyFormatting.fullFormatting(money))
            )))

        if (target.isOnline && creditLite.config.getBoolean("messages.target.notify_set")) {
            target.player?.sendMessage(creditLite.locale.translation("messages.target.set_credit", TagResolver.resolver(
                    Placeholder.parsed("sender", sender.name),
                    Placeholder.parsed("credit", creditLite.creditEconomyFormatting.fullFormatting(money))
                )
            ))
        }
    }

}