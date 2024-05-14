package com.github.encryptsl.credit.common

import com.github.encryptsl.credit.CreditLite
import com.github.encryptsl.credit.api.enums.PurgeKey
import com.github.encryptsl.credit.api.objects.ModernText
import com.github.encryptsl.credit.commands.CreditCmd
import com.github.encryptsl.credit.commands.CreditsCmd
import com.github.encryptsl.credit.common.config.Locales
import com.github.encryptsl.credit.utils.MigrationTool.MigrationKey
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.annotations.AnnotationParser
import org.incendo.cloud.bukkit.CloudBukkitCapabilities
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.minecraft.extras.MinecraftExceptionHandler
import org.incendo.cloud.paper.LegacyPaperCommandManager
import org.incendo.cloud.suggestion.Suggestion
import java.util.concurrent.CompletableFuture

class CommandManager(private val creditLite: CreditLite) {

    fun registerCommands() {
        try {
            creditLite.logger.info("Registering commands with Cloud Command Framework !")

            val commandManager = createCommandManager()

            registerMinecraftExceptionHandler(commandManager)
            registerSuggestionProviders(commandManager)

            val annotationParser = createAnnotationParser(commandManager)
            annotationParser.parse(CreditCmd(creditLite))
            annotationParser.parse(CreditsCmd(creditLite))
        } catch (e : NoClassDefFoundError) {
            creditLite.logger.severe(e.message ?: e.localizedMessage)
        }
    }

    private fun createCommandManager(): LegacyPaperCommandManager<CommandSender> {
        val commandManager = LegacyPaperCommandManager<CommandSender>(
            creditLite,
            ExecutionCoordinator.simpleCoordinator(),
            SenderMapper.identity<CommandSender>()
        )
        if (commandManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            commandManager.registerBrigadier()
            commandManager.brigadierManager().setNativeNumberSuggestions(false)
        } else if (commandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            (commandManager as LegacyPaperCommandManager<*>).registerAsynchronousCompletions()
        }
        return commandManager
    }

    private fun registerMinecraftExceptionHandler(commandManager: LegacyPaperCommandManager<CommandSender>) {
        MinecraftExceptionHandler.createNative<CommandSender>()
            .defaultHandlers()
            .decorator { component ->
                ModernText.miniModernText(creditLite.config.getString("plugin.prefix", "<red>[!]").toString())
                    .appendSpace()
                    .append(component)
            }
            .registerTo(commandManager)
    }

    private fun registerSuggestionProviders(commandManager: LegacyPaperCommandManager<CommandSender>) {
        commandManager.parserRegistry().registerSuggestionProvider("players") { _, _ ->
            CompletableFuture.completedFuture(Bukkit.getOfflinePlayers()
                .map { Suggestion.suggestion(it.name.toString()) }
            )
        }
        commandManager.parserRegistry().registerSuggestionProvider("langKeys") { _, _ ->
            CompletableFuture.completedFuture(Locales.LangKey.entries.map { Suggestion.suggestion(it.name) })
        }
        commandManager.parserRegistry().registerSuggestionProvider("purgeKeys") { _, _ ->
            CompletableFuture.completedFuture(PurgeKey.entries.map { Suggestion.suggestion(it.name) })
        }
        commandManager.parserRegistry().registerSuggestionProvider("migrationKeys") { _, _ ->
            CompletableFuture.completedFuture(MigrationKey.entries.map { Suggestion.suggestion(it.name) })
        }
    }

    private fun createAnnotationParser(commandManager: LegacyPaperCommandManager<CommandSender>): AnnotationParser<CommandSender> {
        return AnnotationParser<CommandSender>(commandManager, CommandSender::class.java)
    }
}