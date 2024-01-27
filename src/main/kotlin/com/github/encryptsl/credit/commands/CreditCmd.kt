package com.github.encryptsl.credit.commands

import org.incendo.cloud.annotation.specifier.Range
import org.incendo.cloud.annotations.*
import com.github.encryptsl.credit.api.Paginator
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
            if (offlinePlayer == null) {
                commandSender.sendMessage(
                    ModernText.miniModernText(
                        creditLite.locale.getMessage("messages.balance.format"),
                        TagResolver.resolver(
                            Placeholder.parsed(
                                "credit",
                                creditLite.api.fullFormatting(creditLite.api.getBalance(commandSender))
                            )
                        )
                    )
                )
                return
            }
            commandSender.sendMessage(
                ModernText.miniModernText(
                    creditLite.locale.getMessage("messages.balance.format_target"),
                    TagResolver.resolver(
                        Placeholder.parsed("target", offlinePlayer.name.toString()),
                        Placeholder.parsed(
                            "credit",
                            creditLite.api.fullFormatting(creditLite.api.getBalance(offlinePlayer))
                        )
                    )
                )
            )
        } else {
            if (offlinePlayer != null) {
                commandSender.sendMessage(
                    ModernText.miniModernText(
                        creditLite.locale.getMessage("messages.balance.format_target"),
                        TagResolver.resolver(
                            Placeholder.parsed("target", offlinePlayer.name.toString()),
                            Placeholder.parsed(
                                "credit",
                                creditLite.api.fullFormatting(creditLite.api.getBalance(offlinePlayer))
                            )
                        )
                    )
                )
                return
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


        val topPlayers = creditLite.api.getTopBalance().toList()
            .sortedByDescending { e -> e.second }.positionIndexed { index, pair ->
                creditLite.locale.getMessage("messages.balance.top_format")
                    .replace("<position>", index.toString())
                    .replace("<player>", Bukkit.getOfflinePlayer(UUID.fromString(pair.first)).name.toString())
                    .replace("<credit>", creditLite.api.fullFormatting(pair.second))
            }
        if (topPlayers.isEmpty()) return

        val pagination = Paginator(topPlayers).apply {
            page(p)
        }

        if (p > pagination.maxPages) {
            commandSender.sendMessage(
                ModernText.miniModernText(creditLite.locale.getMessage("messages.error.maximum_page"),
                    TagResolver.resolver(Placeholder.parsed("max_page", pagination.maxPages.toString())))
            )
            return
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
        if (commandSender is Player) {
            if (commandSender.name == offlinePlayer.name) {
                commandSender.sendMessage(ModernText.miniModernText(creditLite.locale.getMessage("messages.error.self_pay")))
                return
            }

            val amount = helper.validateAmount(amountStr, commandSender) ?: return

            creditLite.server.scheduler.runTask(creditLite) { ->
                creditLite.pluginManager.callEvent(PlayerCreditPayEvent(commandSender, offlinePlayer, amount))
            }
        } else {
            commandSender.sendMessage(ModernText.miniModernText("<red>Only a player can use this command."))
        }
    }
}