package com.github.encryptsl.credit.commands

import com.github.encryptsl.credit.api.enums.CheckLevel
import com.github.encryptsl.credit.api.enums.PurgeKey
import com.github.encryptsl.credit.api.events.*
import com.github.encryptsl.credit.common.config.Locales.LangKey
import com.github.encryptsl.credit.common.extensions.convertInstant
import com.github.encryptsl.credit.common.extensions.getRandomString
import com.github.encryptsl.credit.utils.Helper
import com.github.encryptsl.credit.utils.MigrationTool
import com.github.encryptsl.credit.utils.MigrationTool.MigrationKey
import com.github.encryptsl.kmono.lib.api.ModernText
import com.github.encryptsl.kmono.lib.api.commands.AnnotationFeatures
import com.github.encryptsl.kmono.lib.dependencies.incendo.cloud.annotation.specifier.Range
import com.github.encryptsl.kmono.lib.dependencies.incendo.cloud.annotations.*
import com.github.encryptsl.kmono.lib.dependencies.incendo.cloud.paper.LegacyPaperCommandManager
import com.github.encryptsl.kmono.lib.dependencies.incendo.cloud.suggestion.Suggestion
import com.github.encryptsl.kmono.lib.utils.pagination.ComponentPaginator
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ThreadLocalRandom
import kotlin.system.measureTimeMillis

@Suppress("UNUSED")
@CommandDescription("Provided plugin by CreditLite")
class CreditsCmd(private val creditLite: com.github.encryptsl.credit.CreditLite) : AnnotationFeatures {
    private val helper: Helper = Helper(creditLite)

    override fun registerFeatures(
        annotationParser: AnnotationParser<CommandSender>,
        commandManager: LegacyPaperCommandManager<CommandSender>
    ) {
        commandManager.parserRegistry().registerSuggestionProvider("langKeys") { _, _ ->
            CompletableFuture.completedFuture(LangKey.entries.map { Suggestion.suggestion(it.name) })
        }
        commandManager.parserRegistry().registerSuggestionProvider("purgeKeys") { _, _ ->
            CompletableFuture.completedFuture(PurgeKey.entries.map { Suggestion.suggestion(it.name) })
        }
        commandManager.parserRegistry().registerSuggestionProvider("migrationKeys") { _, _ ->
            CompletableFuture.completedFuture(MigrationKey.entries.map { Suggestion.suggestion(it.name) })
        }
        annotationParser.parse(this)
    }

    @Command("credits help")
    @Permission("credit.admin.help")
    fun adminHelp(commandSender: CommandSender) {
        val list = creditLite.locale.getList("messages.admin-help") ?: return
        for (s in list) {
            commandSender.sendMessage(ModernText.miniModernText(s.toString()))
        }
    }

    @Command("credits add <player> <amount>")
    @Permission("credit.admin.add")
    fun onAddCredit(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer,
        @Argument(value = "amount") @Range(min = "1.00", max = "") amountStr: String,
        @Flag(value = "silent", aliases = ["s"]) silent: Boolean,
    ) {
        val amount = helper.validateAmount(amountStr, commandSender) ?: return
        creditLite.pluginManager.callEvent(CreditDepositEvent(commandSender, offlinePlayer, amount, silent))
    }

    @Command("credits global add <amount>")
    @Permission("credit.admin.global.add")
    fun onGlobalAddCredits(
        commandSender: CommandSender,
        @Argument("amount") @Range(min = "1.0", max = "") amountStr: String
    ) {
        val amount = helper.validateAmount(amountStr, commandSender) ?: return
        creditLite.pluginManager.callEvent(GlobalCreditDepositEvent(commandSender, amount))
    }

    @Command("credits set <player> <amount>")
    @Permission("credit.admin.set")
    fun onSetBalance(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer,
        @Argument(value = "amount") amountStr: String
    ) {
        val amount = helper.validateAmount(amountStr, commandSender, CheckLevel.ONLY_NEGATIVE) ?: return
        creditLite.pluginManager.callEvent(CreditSetEvent(commandSender, offlinePlayer, amount))
    }

    @Command("credits global set <amount>")
    @Permission("credit.admin.global.set")
    fun onGlobalSetCredits(
        commandSender: CommandSender,
        @Argument("amount") @Range(min = "1.0", max = "") amountStr: String
    ) {
        val amount = helper.validateAmount(amountStr, commandSender, CheckLevel.ONLY_NEGATIVE) ?: return
        creditLite.pluginManager.callEvent(GlobalCreditSetEvent(commandSender, amount))
    }

    @Command("credits withdraw <player> <amount>")
    @Permission("credit.admin.withdraw")
    fun onRemoveCredit(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer,
        @Argument(value = "amount") @Range(min = "1.00", max = "") amountStr: String
    ) {
        val amount = helper.validateAmount(amountStr, commandSender) ?: return
        creditLite.pluginManager.callEvent(CreditWithdrawEvent(commandSender, offlinePlayer, amount))
    }

    @Command("credits global remove <amount>")
    @Permission("credit.admin.global.remove")
    fun onGlobalRemoveCredit(
        commandSender: CommandSender,
        @Argument("amount") @Range(min = "1.0", max = "") amountStr: String
    ) {
        val amount = helper.validateAmount(amountStr, commandSender) ?: return

        creditLite.pluginManager.callEvent(GlobalCreditWithdrawEvent(commandSender, amount))
    }

    @Command("credits lang <isoKey>")
    @Permission("credit.admin.lang")
    fun onLangSwitch(
        commandSender: CommandSender,
        @Argument(value = "isoKey", suggestions = "langKeys") isoKey: LangKey
    ) {
        try {
            creditLite.locale.setLocale(isoKey)
            commandSender.sendMessage(
                creditLite.locale.translation("messages.admin.translation_switch",
                    Placeholder.parsed("locale", isoKey.name)
                )
            )
        } catch (_: IllegalArgumentException) {
            commandSender.sendMessage(ModernText.miniModernText("That translation doesn't exist."))
        }
    }

    @Command("credits purge <argument>")
    @Permission("credit.admin.purge")
    fun onPurge(commandSender: CommandSender, @Argument(value = "argument", suggestions = "purgeKeys") purgeKey: PurgeKey)
    {
        @Suppress("REDUNDANT_ELSE_IN_WHEN")
        when (purgeKey) {
            PurgeKey.ACCOUNTS -> {
                creditLite.creditModel.purgeAccounts()
                commandSender.sendMessage(creditLite.locale.translation("messages.admin.purge_accounts"))
            }
            PurgeKey.NULL_ACCOUNTS -> {
                creditLite.creditModel.purgeInvalidAccounts()
                commandSender.sendMessage(creditLite.locale.translation("messages.admin.purge_null_accounts"))
            }
            PurgeKey.DEFAULT_ACCOUNTS -> {
                creditLite.creditModel.purgeDefaultAccounts(creditLite.config.getInt("economy.starting_balance").toBigDecimal())
                commandSender.sendMessage(creditLite.locale.translation("messages.admin.purge_default_accounts"))
            }
            PurgeKey.MONO_LOG -> {
                creditLite.monologModel.getLog().thenApply { el ->
                    if (el.isEmpty())
                        throw Exception("You can't delete monolog because is empty !")
                    return@thenApply el
                }.thenApply { el ->
                    creditLite.monologModel.clearLogs()
                    commandSender.sendMessage(creditLite.locale.translation("messages.admin.purge_monolog_success", Placeholder.parsed("deleted", el.size.toString())))
                }.exceptionally { el ->
                    commandSender.sendMessage(creditLite.locale.translation("messages.error.purge_monolog_fail"))
                    creditLite.logger.severe(el.message ?: el.localizedMessage)
                }
            }
            else -> {
                commandSender.sendMessage(creditLite.locale.translation("messages.error.purge_argument"))
            }
        }
    }

    @Command("credits monolog [page] [player]")
    @Permission("credit.admin.monolog")
    fun onLogView(commandSender: CommandSender, @Argument("page") @Default(value = "1") page: Int, @Argument("player") player: String?) {
        val log = helper.validateLog(player).map {
            creditLite.locale.translation("messages.admin.monolog_format", TagResolver.resolver(
                Placeholder.parsed("level", it.level),
                Placeholder.parsed("timestamp", convertInstant(it.timestamp)),
                Placeholder.parsed("log", it.log)
            ))
        }
        val pagination = ComponentPaginator(log).apply { page(1) }

        if (pagination.isAboveMaxPage(page))
            return commandSender.sendMessage(creditLite.locale.translation("messages.error.maximum_page",
                Placeholder.parsed("max_page", pagination.maxPages.toString()))
            )

        for (content in pagination.display()) { commandSender.sendMessage(content) }
    }

    @Command("credits migration <argument>")
    @Permission("credit.admin.migration")
    fun onMigration(commandSender: CommandSender, @Argument(value = "argument", suggestions = "migrationKeys") migrationKey: MigrationKey) {
        val migrationTool = MigrationTool(creditLite)

        val output = helper.getAccountsToMigrationData()

        val result = when(migrationKey) {
            MigrationKey.CSV -> output.let { migrationTool.migrateToCSV(it, "economy_migration") }
            MigrationKey.SQL -> output.let { migrationTool.migrateToSQL(it, "economy_migration") }
        }

        val messageKey = if (result) {
            "messages.admin.migration_success"
        } else {
            "messages.error.migration_failed"
        }

        commandSender.sendMessage(creditLite.locale.translation(messageKey, Placeholder.parsed("type", migrationKey.name)))
    }

    @Command("credits debug create accounts <amount>")
    @Permission("credit.admin.debug.create.accounts")
    fun onDebugCreateAccounts(commandSender: CommandSender, @Argument("amount") @Range(min = "1", max = "100") amountStr: Int) {

        val random = ThreadLocalRandom.current()

        val time = measureTimeMillis {
            for (i in 1 .. amountStr) {
                creditLite.creditModel.createPlayerAccount(getRandomString(6), UUID.randomUUID(), random.nextDouble(1000.0, 500000.0).toBigDecimal())
            }
        }

        commandSender.sendMessage("Into database was inserted $amountStr fake accounts in time $time ms")
    }

    @Command("credits debug database accounts")
    @Permission("credit.admin.debug.database.accounts")
    fun onDebugDatabase(commandSender: CommandSender) {

        val data = creditLite.creditModel.getTopBalance()

        commandSender.sendMessage("Accounts list " + data.entries.map { k -> k.key })
        commandSender.sendMessage("Accounts balances " + data.entries.sumOf { it.value })
        commandSender.sendMessage("Accounts in database " + data.size.toString())
    }

    @Command("credits reload")
    @Permission("credit.admin.reload")
    fun onReload(commandSender: CommandSender) {
        creditLite.reloadConfig()
        commandSender.sendMessage(creditLite.locale.translation("messages.admin.config_reload"))
        creditLite.logger.info("Config.yml was reloaded [!]")
        creditLite.saveConfig()
        creditLite.locale.loadCurrentTranslation()
    }
}