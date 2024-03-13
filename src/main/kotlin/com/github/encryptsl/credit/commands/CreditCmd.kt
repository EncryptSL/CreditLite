package com.github.encryptsl.credit.commands

import org.incendo.cloud.annotation.specifier.Range
import org.incendo.cloud.annotations.*
import com.github.encryptsl.credit.api.Paginator
import com.github.encryptsl.credit.api.economy.CreditEconomy
import com.github.encryptsl.credit.api.events.PlayerCreditPayEvent
import com.github.encryptsl.credit.api.objects.ModernText
import com.github.encryptsl.credit.extensions.positionIndexed
import com.github.encryptsl.credit.utils.Helper
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

@Suppress("UNUSED")
@CommandDescription("Provided plugin by CreditLite")
class CreditCmd(private val creditLite: com.github.encryptsl.credit.CreditLite) {
    private val helper: Helper = Helper(creditLite)

    @Command("credit help")
    @Permission("credit.help")
    fun onHelp(commandSender: CommandSender) {
        creditLite.locale.getList("messages.help")?.forEach { s ->
            commandSender.sendMessage(ModernText.miniModernText(s.toString()))
        }
    }

    @Command("cbal|cbalance [player]")
    @Permission("credit.balance")
    fun onBalanceProxy(commandSender: CommandSender, @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer?) {
        onBalance(commandSender, offlinePlayer)
    }

    @Command("credit bal [player]")
    @Permission("credit.balance")
    fun onBalance(commandSender: CommandSender, @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer?) {
        if (commandSender is Player) {
            val formatMessage = when(offlinePlayer) {
                null -> creditLite.locale.getMessage("messages.balance.format")
                else -> creditLite.locale.getMessage("messages.balance.format_target")
            }
            val cSender = offlinePlayer ?: commandSender

            if (!CreditEconomy.hasAccount(cSender))
                return commandSender.sendMessage(ModernText.miniModernText(creditLite.locale.getMessage("messages.error.account_not_exist"),
                    TagResolver.resolver(Placeholder.parsed("account", cSender.name.toString()))))

            commandSender.sendMessage(ModernText.miniModernText(formatMessage, helper.getComponentBal(cSender)))
        } else {
            offlinePlayer?.let {
                if (!CreditEconomy.hasAccount(it))
                    return commandSender.sendMessage(ModernText.miniModernText(creditLite.locale.getMessage("messages.error.account_not_exist"),
                        TagResolver.resolver(Placeholder.parsed("account", it.name.toString()))))

                return commandSender.sendMessage(ModernText.miniModernText(
                    creditLite.locale.getMessage("messages.balance.format_target"), helper.getComponentBal(it)))
            }
            creditLite.locale.getList("messages.help")?.forEach { s ->
                commandSender.sendMessage(ModernText.miniModernText(s.toString()))
            }
        }
    }

    @ProxiedBy("cbaltop")
    @Command("credit top [page]")
    @Permission("credit.top")
    fun onTopBalance(commandSender: CommandSender, @Argument(value = "page") @Range(min = "1", max="") page: Int?) {
        val p = page ?: 1

        val topPlayers = CreditEconomy.getTopBalance().toList()
            .sortedByDescending { e -> e.second }.positionIndexed { index, pair ->
                creditLite.locale.getMessage("messages.balance.top_format")
                    .replace("<position>", index.toString())
                    .replace("<player>", Bukkit.getOfflinePlayer(UUID.fromString(pair.first)).name.toString())
                    .replace("<credit>", creditLite.creditEconomyFormatting.fullFormatting(pair.second))
            }
        if (topPlayers.isEmpty()) return

        val pagination = Paginator(topPlayers).apply {
            page(p)
        }

        if (p > pagination.maxPages) {
            return commandSender.sendMessage(ModernText.miniModernText(creditLite.locale.getMessage("messages.error.maximum_page"), TagResolver
                .resolver(Placeholder.parsed("max_page", pagination.maxPages.toString())))
            )
        }

        commandSender.sendMessage(
            ModernText.miniModernText(
                creditLite.locale.getMessage("messages.balance.top_header"),
                TagResolver.resolver(
                    Placeholder.parsed("page", pagination.page().toString()), Placeholder.parsed("max_page", pagination.maxPages.toString())
                ))
                .appendNewline().append(ModernText.miniModernText(pagination.display()))
                .appendNewline().append(ModernText.miniModernText(creditLite.locale.getMessage("messages.balance.top_footer")))
        )
    }

    @ProxiedBy("cpay")
    @Command("credit pay <player> <amount>")
    @Permission("credit.pay")
    fun onPayCredit(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer,
        @Argument(value = "amount") @Range(min = "1.00", max = "") amountStr: String
    ) {
        if (commandSender !is Player)
            return commandSender.sendMessage(ModernText.miniModernText("<red>Only a player can use this command."))

        if (commandSender.name == offlinePlayer.name)
            return commandSender.sendMessage(ModernText.miniModernText(creditLite.locale.getMessage("messages.error.self_pay")))

        val amount = helper.validateAmount(amountStr, commandSender) ?: return

        creditLite.pluginManager.callEvent(PlayerCreditPayEvent(commandSender, offlinePlayer, amount))
    }
}