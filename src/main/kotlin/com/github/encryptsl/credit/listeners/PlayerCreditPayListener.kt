package com.github.encryptsl.credit.listeners

import com.github.encryptsl.credit.api.economy.CreditEconomy
import com.github.encryptsl.credit.api.events.PlayerCreditPayEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PlayerCreditPayListener(private val creditLite: com.github.encryptsl.credit.CreditLite) : Listener {
    @EventHandler
    fun onEconomyPay(event: PlayerCreditPayEvent) {
        val sender: Player = event.sender
        val target: OfflinePlayer = event.target
        val money: Double = event.money

        if (!CreditEconomy.hasAccount(target.uniqueId))
            return sender.sendMessage(creditLite.locale.translation("messages.error.account_not_exist",
                Placeholder.parsed("account", target.name.toString())))

        if (!CreditEconomy.has(sender.uniqueId, money))
            return sender.sendMessage(creditLite.locale.translation("messages.error.insufficient_funds"))

        CreditEconomy.withdraw(sender.uniqueId, money)
        CreditEconomy.deposit(target.uniqueId, money)
        creditLite.monologModel.info(creditLite.locale.getMessage("messages.monolog.player.pay")
            .replace("<sender>", sender.name)
            .replace("<target>", target.name.toString())
            .replace("<credit>", creditLite.creditEconomyFormatting.fullFormatting(money))
        )

        sender.sendMessage(creditLite.locale.translation("messages.sender.add_credit", TagResolver.resolver(
            Placeholder.parsed("target", target.name.toString()),
            Placeholder.parsed("credit", creditLite.creditEconomyFormatting.fullFormatting(money))
        )))
        if (target.isOnline) {
            target.player?.sendMessage(creditLite.locale.translation("messages.target.add_credit",
                TagResolver.resolver(
                    Placeholder.parsed("sender", sender.name),
                    Placeholder.parsed("credit", creditLite.creditEconomyFormatting.fullFormatting(money))
                )
            ))
        }
    }
}