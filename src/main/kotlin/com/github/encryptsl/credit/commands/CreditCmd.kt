package com.github.encryptsl.credit.commands

import com.github.encryptsl.credit.api.economy.CreditEconomy
import com.github.encryptsl.credit.api.events.PlayerCreditPayEvent
import com.github.encryptsl.credit.utils.Helper
import com.github.encryptsl.kmono.lib.api.ModernText
import com.github.encryptsl.kmono.lib.api.commands.AnnotationFeatures
import com.github.encryptsl.kmono.lib.utils.ComponentPaginator
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.annotation.specifier.Range
import org.incendo.cloud.annotations.*
import org.incendo.cloud.paper.LegacyPaperCommandManager

@Suppress("UNUSED")
@CommandDescription("Provided plugin by CreditLite")
class CreditCmd(private val creditLite: com.github.encryptsl.credit.CreditLite) : AnnotationFeatures {

    private val helper: Helper = Helper(creditLite)

    override fun registerFeatures(
        annotationParser: AnnotationParser<CommandSender>,
        commandManager: LegacyPaperCommandManager<CommandSender>
    ) {
        annotationParser.parse(this)
    }

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
            val cSender = offlinePlayer ?: commandSender

           CreditEconomy.getUserByUUID(cSender).thenApply { user ->
               val formatMessage = when(offlinePlayer) {
                   null -> creditLite.locale.translation("messages.balance.format", helper.getComponentBal(user))
                   else -> creditLite.locale.translation("messages.balance.format_target", helper.getComponentBal(user))
               }
               commandSender.sendMessage(formatMessage)
           }.exceptionally {
               commandSender.sendMessage(creditLite.locale.translation("messages.error.account_not_exist",
                   Placeholder.parsed("account", cSender.name.toString())))
           }
            return
        }
        if (offlinePlayer != null) {
            CreditEconomy.getUserByUUID(offlinePlayer).thenApply { user ->
                commandSender.sendMessage(
                    creditLite.locale.translation("messages.balance.format_target", helper.getComponentBal(user))
                )
            }.exceptionally {
                commandSender.sendMessage(creditLite.locale.translation("messages.error.account_not_exist",
                    Placeholder.parsed("account", offlinePlayer.name.toString())))
            }
            return
        }
        creditLite.locale.getList("messages.help")
            ?.forEach { s -> commandSender.sendMessage(ModernText.miniModernText(s.toString())) }
    }

    @ProxiedBy("cbaltop")
    @Command("credit top [page]")
    @Permission("credit.top")
    fun onTopBalance(commandSender: CommandSender, @Argument(value = "page") @Default("1") page: Int) {
        val topPlayers = helper.getTopBalances()

        val paginator = ComponentPaginator(topPlayers).apply { page(page) }

        if (paginator.isAboveMaxPage(page))
            return commandSender.sendMessage(creditLite.locale.translation("messages.error.maximum_page",
                Placeholder.parsed("max_page", paginator.maxPages.toString()))
            )

        for (component in paginator.display()) {
            commandSender.sendMessage(
                creditLite.locale.translation("messages.balance.top_header",
                    TagResolver.resolver(
                        Placeholder.parsed("page", paginator.currentPage().toString()), Placeholder.parsed("max_page", paginator.maxPages.toString())
                    ))
                    .appendNewline()
                    .append(component)
                    .appendNewline()
                    .append(creditLite.locale.translation("messages.balance.top_footer"))
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