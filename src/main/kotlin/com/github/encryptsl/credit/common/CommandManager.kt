package com.github.encryptsl.credit.common

import com.github.encryptsl.credit.CreditLite
import com.github.encryptsl.credit.commands.CreditCmd
import com.github.encryptsl.credit.commands.CreditsCmd
import com.github.encryptsl.kmono.lib.api.ModernText
import com.github.encryptsl.kmono.lib.api.commands.AnnotationCommandRegister
import com.github.encryptsl.kmono.lib.dependencies.incendo.cloud.SenderMapper
import com.github.encryptsl.kmono.lib.dependencies.incendo.cloud.annotations.AnnotationParser
import com.github.encryptsl.kmono.lib.dependencies.incendo.cloud.bukkit.CloudBukkitCapabilities
import com.github.encryptsl.kmono.lib.dependencies.incendo.cloud.execution.ExecutionCoordinator
import com.github.encryptsl.kmono.lib.dependencies.incendo.cloud.minecraft.extras.MinecraftExceptionHandler
import com.github.encryptsl.kmono.lib.dependencies.incendo.cloud.paper.LegacyPaperCommandManager
import com.github.encryptsl.kmono.lib.dependencies.incendo.cloud.suggestion.Suggestion
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import java.util.concurrent.CompletableFuture

class CommandManager(private val creditLite: CreditLite) {

    fun registerCommands() {
        try {
            creditLite.logger.info("Registering commands with Cloud Command Framework !")

            val commandManager = createCommandManager()

            registerMinecraftExceptionHandler(commandManager)
            registerSuggestionProviders(commandManager)

            val annotationParser = createAnnotationParser(commandManager)
            val register = AnnotationCommandRegister(creditLite, annotationParser, commandManager)
            register.registerCommand(CreditCmd(creditLite), CreditsCmd(creditLite))
        } catch (e : NoClassDefFoundError) {
            creditLite.logger.severe(e.message ?: e.localizedMessage)
        }
    }

    private fun createCommandManager(): LegacyPaperCommandManager<CommandSender> {
        val commandManager = LegacyPaperCommandManager(
            creditLite,
            ExecutionCoordinator.simpleCoordinator(),
            SenderMapper.identity()
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
    }

    private fun createAnnotationParser(commandManager: LegacyPaperCommandManager<CommandSender>): AnnotationParser<CommandSender> {
        return AnnotationParser(commandManager, CommandSender::class.java)
    }
}