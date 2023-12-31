package com.github.encryptsl.credit.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.specifier.Range
import com.github.encryptsl.credit.api.enums.CheckLevel
import com.github.encryptsl.credit.api.enums.LangKey
import com.github.encryptsl.credit.api.enums.MigrationKey
import com.github.encryptsl.credit.api.enums.PurgeKey
import com.github.encryptsl.credit.api.events.*
import com.github.encryptsl.credit.api.objects.ModernText
import com.github.encryptsl.credit.extensions.positionIndexed
import com.github.encryptsl.credit.utils.Helper
import com.github.encryptsl.credit.utils.MigrationData
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

    @CommandMethod("credits help")
    @CommandPermission("credit.admin.help")
    fun adminHelp(commandSender: CommandSender) {
        creditLite.locale.getList("messages.admin-help")?.forEach { s ->
            commandSender.sendMessage(ModernText.miniModernText(s.toString()))
        }
    }

    @CommandMethod("credits add <player> <amount> [silent]")
    @CommandPermission("credit.admin.add")
    fun onAddCredit(
        commandSender: CommandSender,
        @Argument(value = "silent") silent: String?,
        @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer,
        @Argument(value = "amount") @Range(min = "1.00", max = "") amountStr: String,
    ) {
        val amount = helper.validateAmount(amountStr, commandSender) ?: return
        val s = silent?.contains("-s") ?: false || silent?.contains("-silent") ?: false

        creditLite.server.scheduler.runTask(creditLite) { ->
            creditLite.pluginManager.callEvent(CreditDepositEvent(commandSender, offlinePlayer, amount, s))
        }
    }

    @CommandMethod("credits gadd <amount>")
    @CommandPermission("credit.admin.gadd")
    fun onGlobalAddCredits(
        commandSender: CommandSender,
        @Argument("amount") @Range(min = "1.0", max = "") amountStr: String
    ) {
        val amount = helper.validateAmount(amountStr, commandSender) ?: return

        creditLite.server.scheduler.runTask(creditLite) { ->
            creditLite.pluginManager.callEvent(
                GlobalCreditDepositEvent(commandSender, amount)
            )
        }
    }

    @CommandMethod("credits set <player> <amount>")
    @CommandPermission("credit.admin.set")
    fun onSetBalance(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer,
        @Argument(value = "amount") amountStr: String
    ) {
        val amount = helper.validateAmount(amountStr, commandSender, CheckLevel.ONLY_NEGATIVE) ?: return

        creditLite.server.scheduler.runTask(creditLite) { ->
            creditLite.pluginManager.callEvent(
                CreditSetEvent(
                    commandSender,
                    offlinePlayer,
                    amount
                )
            )
        }
    }

    @CommandMethod("credits gset <amount>")
    @CommandPermission("credit.admin.gset")
    fun onGlobalSetCredits(
        commandSender: CommandSender,
        @Argument("amount") @Range(min = "1.0", max = "") amountStr: String
    ) {
        val amount = helper.validateAmount(amountStr, commandSender, CheckLevel.ONLY_NEGATIVE) ?: return

        creditLite.server.scheduler.runTask(creditLite) { ->
            creditLite.pluginManager.callEvent(
                GlobalCreditSetEvent(commandSender, amount)
            )
        }
    }

    @CommandMethod("credits remove <player> <amount>")
    @CommandPermission("credit.admin.remove")
    fun onRemoveCredit(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer,
        @Argument(value = "amount") @Range(min = "1.00", max = "") amountStr: String
    ) {
        val amount = helper.validateAmount(amountStr, commandSender) ?: return

        creditLite.server.scheduler.runTask(creditLite) { ->
            creditLite.pluginManager.callEvent(
                CreditWithdrawEvent(
                    commandSender,
                    offlinePlayer,
                    amount
                )
            )
        }
    }

    @CommandMethod("credits gremove <amount>")
    @CommandPermission("credit.admin.gremove")
    fun onGlobalRemoveCredit(
        commandSender: CommandSender,
        @Argument("amount") @Range(min = "1.0", max = "") amountStr: String
    ) {
        val amount = helper.validateAmount(amountStr, commandSender) ?: return

        creditLite.server.scheduler.runTask(creditLite) { ->
            creditLite.pluginManager.callEvent(
                GlobalCreditWithdrawEvent(commandSender, amount)
            )
        }
    }

    @CommandMethod("credits lang <isoKey>")
    @CommandPermission("credit.admin.lang")
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
            commandSender.sendMessage(
                ModernText.miniModernText(
                    "That translation doesn't exist."
                )
            )
        }
    }

    @CommandMethod("credits purge <argument>")
    @CommandPermission("credit.admin.purge")
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

    @CommandMethod("credits migration <argument>")
    @CommandPermission("credit.admin.migration")
    fun onMigration(commandSender: CommandSender, @Argument(value = "argument", suggestions = "migrationKeys") migrationKey: MigrationKey) {
        val migrationTool = MigrationTool(creditLite)
        val output = creditLite.api.getTopBalance().toList().positionIndexed { index, k -> MigrationData(index, k.first, k.second) }

        val result = when(migrationKey) {
            MigrationKey.CSV -> migrationTool.migrateToCSV(output, "economy_migration")
            MigrationKey.SQL -> migrationTool.migrateToSQL(output, "economy_migration")
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

    @CommandMethod("credits debug create accounts <amount>")
    @CommandPermission("credit.admin.debug.create.accounts")
    fun onDebugCreateAccounts(commandSender: CommandSender, @Argument("amount") @Range(min = "1", max = "100") amountStr: Int) {

        val random = ThreadLocalRandom.current()

        val time = measureTimeMillis {
            for (i in 1 .. amountStr) {
                creditLite.creditModel.createPlayerAccount("", UUID.randomUUID(), random.nextDouble(1000.0, 500000.0))
            }
        }

        commandSender.sendMessage("Into database was insterted $amountStr fake accounts in time $time ms")
    }

    @CommandMethod("credits reload")
    @CommandPermission("credit.admin.reload")
    fun onReload(commandSender: CommandSender) {
        creditLite.reloadConfig()
        commandSender.sendMessage(ModernText.miniModernText(creditLite.locale.getMessage("messages.admin.config_reload")))
        creditLite.logger.info("Config.yml was reloaded [!]")
        creditLite.saveConfig()
        creditLite.locale.reloadTranslation()
    }
}