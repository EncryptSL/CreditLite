package com.github.encryptsl.credit.listeners.admin

import com.github.encryptsl.credit.api.economy.CreditEconomy
import com.github.encryptsl.credit.api.events.GlobalCreditSetEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class GlobalCreditSetListener(private val creditLite: com.github.encryptsl.credit.CreditLite) : Listener {
    @EventHandler
    fun onAdminEconomyGlobalSet(event: GlobalCreditSetEvent) {
        val sender: CommandSender = event.commandSender
        val money = event.money
        val offlinePlayers = Bukkit.getOfflinePlayers()

        for (player in offlinePlayers) {
            CreditEconomy.hasAccount(player.uniqueId).thenAccept {
                CreditEconomy.set(player, money)
            }
        }

        creditLite.monologModel.info(creditLite.locale.getMessage("messages.monolog.admin.global.set")
            .replace("<sender>", sender.name)
            .replace("<accounts", offlinePlayers.size.toString())
            .replace("<credit>", creditLite.creditEconomyFormatting.fullFormatting(money))
        )

        sender.sendMessage(creditLite.locale.translation("messages.global.set_credit",
            Placeholder.parsed("money", creditLite.creditEconomyFormatting.fullFormatting(money))
        ))
        if (creditLite.config.getBoolean("messages.global.notify_set"))
            Bukkit.broadcast(creditLite.locale.translation("messages.broadcast.set_credit", TagResolver.resolver(
                Placeholder.parsed("sender", sender.name),
                Placeholder.parsed("credit", creditLite.creditEconomyFormatting.fullFormatting(money))
            )))
    }
}