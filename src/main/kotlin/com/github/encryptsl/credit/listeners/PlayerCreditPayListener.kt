package com.github.encryptsl.credit.listeners

import com.github.encryptsl.credit.api.economy.CreditEconomy
import com.github.encryptsl.credit.api.events.PlayerCreditPayEvent
import com.github.encryptsl.credit.api.objects.ModernText
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

        if (!CreditEconomy.hasAccount(target)) {
            sender.sendMessage(ModernText.miniModernText(creditLite.locale.getMessage("messages.error.account_not_exist"),
                TagResolver.resolver(Placeholder.parsed("account", target.name.toString()))))
            return
        }
        if (!CreditEconomy.has(sender, money)) {
            sender.sendMessage(ModernText.miniModernText(creditLite.locale.getMessage("messages.error.insufficient_funds")))
            return
        }
        CreditEconomy.withdraw(sender, money)
        CreditEconomy.deposit(target, money)
        creditLite.countTransactions["transactions"] = creditLite.countTransactions.getOrDefault("transactions", 0) + 1
        sender.sendMessage(
            ModernText.miniModernText(
                creditLite.locale.getMessage("messages.sender.add_credit"),
                TagResolver.resolver(Placeholder.parsed("target", target.name.toString()), Placeholder.parsed("credit", creditLite.creditEconomyFormatting.fullFormatting(money)))))
        if (target.isOnline) {
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