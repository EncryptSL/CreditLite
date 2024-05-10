package com.github.encryptsl.credit.listeners.admin

import com.github.encryptsl.credit.api.economy.CreditEconomy
import com.github.encryptsl.credit.api.events.GlobalCreditDepositEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class GlobalCreditDepositListener(private val creditLite: com.github.encryptsl.credit.CreditLite) : Listener {

    @EventHandler
    fun onAdminEconomyGlobalDeposit(event: GlobalCreditDepositEvent) {
        val sender: CommandSender = event.commandSender
        val money = event.money
        val offlinePlayers = Bukkit.getOfflinePlayers()

        for (player in offlinePlayers) {
            if (!CreditEconomy.hasAccount(player.uniqueId)) continue

            CreditEconomy.deposit(player.uniqueId, money)
        }

        creditLite.monologModel.info(creditLite.locale.getMessage("messages.monolog.admin.global.deposit")
            .replace("<sender>", sender.name)
            .replace("<accounts", offlinePlayers.size.toString())
            .replace("<credit>", creditLite.creditEconomyFormatting.fullFormatting(money))
        )


        sender.sendMessage(creditLite.locale.translation("messages.global.add_credit",
            Placeholder.parsed("credit", creditLite.creditEconomyFormatting.fullFormatting(money))
        ))
        if (!creditLite.config.getBoolean("messages.global.notify_add")) {
            Bukkit.broadcast(
                creditLite.locale.translation("messages.broadcast.add_credit", TagResolver.resolver(
                    Placeholder.parsed("sender", sender.name),
                    Placeholder.parsed("credit", creditLite.creditEconomyFormatting.fullFormatting(money))
                )
            ))
        }
    }

}