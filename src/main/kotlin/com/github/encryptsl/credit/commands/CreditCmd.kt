package com.github.encryptsl.credit.commands

import com.github.encryptsl.credit.api.economy.CreditEconomy
import com.github.encryptsl.credit.api.events.PlayerCreditPayEvent
import com.github.encryptsl.credit.api.objects.ModernText
import com.github.encryptsl.credit.utils.Helper
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.ChatPaginator
import org.incendo.cloud.annotation.specifier.Range
import org.incendo.cloud.annotations.*

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
                null -> creditLite.locale.translation("messages.balance.format", helper.getComponentBal(commandSender))
                else -> creditLite.locale.translation("messages.balance.format_target", helper.getComponentBal(offlinePlayer))
            }
            val cSender = offlinePlayer ?: commandSender

            if (!CreditEconomy.hasAccount(cSender.uniqueId))
                return commandSender.sendMessage(creditLite.locale.translation("messages.error.account_not_exist",
                   Placeholder.parsed("account", cSender.name.toString())))

            commandSender.sendMessage(formatMessage)
        } else {
            offlinePlayer?.let {
                if (!CreditEconomy.hasAccount(it.uniqueId))
                    return commandSender.sendMessage(creditLite.locale.translation("messages.error.account_not_exist",
                        Placeholder.parsed("account", it.name.toString())))

                return commandSender.sendMessage(
                    creditLite.locale.translation("messages.balance.format_target", helper.getComponentBal(it)))
            }
            creditLite.locale.getList("messages.help")?.forEach { s ->
                commandSender.sendMessage(ModernText.miniModernText(s.toString()))
            }
        }
    }

    @ProxiedBy("cbaltop")
    @Command("credit top [page]")
    @Permission("credit.top")
    fun onTopBalance(commandSender: CommandSender, @Argument(value = "page") @Default("1") page: Int) {
        val topPlayers = helper.getTopBalances()

        if (topPlayers.isEmpty()) return

        val paginator = ChatPaginator.paginate(topPlayers.joinToString("\n"), page)

        if (page > paginator.totalPages)
            return commandSender.sendMessage(creditLite.locale.translation("messages.error.maximum_page",
                Placeholder.parsed("max_page", paginator.totalPages.toString()))
            )

        for (p in paginator.lines) {
            commandSender.sendMessage(
                creditLite.locale.translation("messages.balance.top_header",
                    TagResolver.resolver(
                        Placeholder.parsed("page", paginator.pageNumber.toString()), Placeholder.parsed("max_page", paginator.totalPages.toString())
                    ))
                    .appendNewline().append(ModernText.miniModernText(p))
                    .appendNewline().append(creditLite.locale.translation("messages.balance.top_footer"))
            )
        }
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
            return commandSender.sendMessage(creditLite.locale.translation("messages.error.self_pay"))

        val amount = helper.validateAmount(amountStr, commandSender) ?: return

        creditLite.pluginManager.callEvent(PlayerCreditPayEvent(commandSender, offlinePlayer, amount))
    }
}