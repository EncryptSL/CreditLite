package com.github.encryptsl.credit.commands

import org.incendo.cloud.annotation.specifier.Range
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.Flag
import org.incendo.cloud.annotations.Permission
import com.github.encryptsl.credit.api.enums.CheckLevel
import com.github.encryptsl.credit.api.enums.LangKey
import com.github.encryptsl.credit.api.enums.MigrationKey
import com.github.encryptsl.credit.api.enums.PurgeKey
import com.github.encryptsl.credit.api.events.*
import com.github.encryptsl.credit.api.objects.ModernText
import com.github.encryptsl.credit.utils.Helper
import com.github.encryptsl.credit.utils.MigrationTool
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.system.measureTimeMillis

@Suppress("UNUSED")
@CommandDescription("Provided plugin by CreditLite")
class CreditsCmd(private val creditLite: com.github.encryptsl.credit.CreditLite) {
    private val helper: Helper = Helper(creditLite)

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

    @Command("credits gadd <amount>")
    @Permission("credit.admin.gadd")
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

    @Command("credits gset <amount>")
    @Permission("credit.admin.gset")
    fun onGlobalSetCredits(
        commandSender: CommandSender,
        @Argument("amount") @Range(min = "1.0", max = "") amountStr: String
    ) {
        val amount = helper.validateAmount(amountStr, commandSender, CheckLevel.ONLY_NEGATIVE) ?: return
        creditLite.pluginManager.callEvent(GlobalCreditSetEvent(commandSender, amount))
    }

    @Command("credits remove <player> <amount>")
    @Permission("credit.admin.remove")
    fun onRemoveCredit(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer,
        @Argument(value = "amount") @Range(min = "1.00", max = "") amountStr: String
    ) {
        val amount = helper.validateAmount(amountStr, commandSender) ?: return
        creditLite.pluginManager.callEvent(CreditWithdrawEvent(commandSender, offlinePlayer, amount))
    }

    @Command("credits gremove <amount>")
    @Permission("credit.admin.gremove")
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
        @Argument(value = "isoKey", suggestions = "langKeys") isoKey: String
    ) {
        try {
            val langKey = LangKey.valueOf(isoKey.uppercase())
            creditLite.locale.setTranslationFile(langKey)
            commandSender.sendMessage(
                ModernText.miniModernText(
                    creditLite.locale.getMessage("messages.admin.translation_switch"),
                    TagResolver.resolver(Placeholder.parsed("locale", langKey.name))
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
                commandSender.sendMessage(ModernText.miniModernText(creditLite.locale.getMessage("messages.admin.purge_accounts")))
            }
            PurgeKey.NULL_ACCOUNTS -> {
                creditLite.creditModel.purgeInvalidAccounts()
                commandSender.sendMessage(ModernText.miniModernText(creditLite.locale.getMessage("messages.admin.purge_null_accounts")))
            }
            PurgeKey.DEFAULT_ACCOUNTS -> {
                creditLite.creditModel.purgeDefaultAccounts(creditLite.config.getDouble("economy.starting_balance"))
                commandSender.sendMessage(ModernText.miniModernText(creditLite.locale.getMessage("messages.admin.purge_default_accounts")))
            }
            else -> {
                commandSender.sendMessage(ModernText.miniModernText(creditLite.locale.getMessage("messages.error.purge_argument")))
            }
        }
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

        commandSender.sendMessage(ModernText.miniModernText(
            creditLite.locale.getMessage(messageKey),
            TagResolver.resolver(
                Placeholder.parsed("type", migrationKey.name)
            )
        ))
    }

    @Command("credits debug create accounts <amount>")
    @Permission("credit.admin.debug.create.accounts")
    fun onDebugCreateAccounts(commandSender: CommandSender, @Argument("amount") @Range(min = "1", max = "100") amountStr: Int) {

        val random = ThreadLocalRandom.current()

        val time = measureTimeMillis {
            for (i in 1 .. amountStr) {
                creditLite.creditModel.createPlayerAccount("", UUID.randomUUID(), random.nextDouble(1000.0, 500000.0))
            }
        }

        commandSender.sendMessage("Into database was insterted $amountStr fake accounts in time $time ms")
    }

    @Command("credits reload")
    @Permission("credit.admin.reload")
    fun onReload(commandSender: CommandSender) {
        creditLite.reloadConfig()
        commandSender.sendMessage(ModernText.miniModernText(creditLite.locale.getMessage("messages.admin.config_reload")))
        creditLite.logger.info("Config.yml was reloaded [!]")
        creditLite.saveConfig()
        creditLite.locale.reloadTranslation()
    }
}