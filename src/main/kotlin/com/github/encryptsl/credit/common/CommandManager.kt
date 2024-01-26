package com.github.encryptsl.credit.common

import cloud.commandframework.SenderMapper
import cloud.commandframework.annotations.AnnotationParser
import cloud.commandframework.bukkit.CloudBukkitCapabilities
import cloud.commandframework.execution.ExecutionCoordinator
import cloud.commandframework.paper.PaperCommandManager
import cloud.commandframework.suggestion.Suggestion
import com.github.encryptsl.credit.CreditLite
import com.github.encryptsl.credit.api.enums.LangKey
import com.github.encryptsl.credit.api.enums.MigrationKey
import com.github.encryptsl.credit.api.enums.PurgeKey
import com.github.encryptsl.credit.commands.CreditCmd
import com.github.encryptsl.credit.commands.CreditsCmd
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import java.util.concurrent.CompletableFuture

class CommandManager(private val creditLite: CreditLite) {

    fun registerCommands() {
        creditLite.logger.info("Registering commands with Cloud Command Framework !")

        val commandManager = createCommandManager()

        registerSuggestionProviders(commandManager)

        val annotationParser = createAnnotationParser(commandManager)
        annotationParser.parse(CreditCmd(creditLite))
        annotationParser.parse(CreditsCmd(creditLite))
    }

    private fun createCommandManager(): PaperCommandManager<CommandSender> {
        val executionCoordinatorFunction = ExecutionCoordinator.builder<CommandSender>().build()
        val mapperFunction = SenderMapper.identity<CommandSender>()
        val commandManager = PaperCommandManager(
            creditLite,
            executionCoordinatorFunction,
            mapperFunction
        )
        if (commandManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            commandManager.registerBrigadier()
            commandManager.brigadierManager().setNativeNumberSuggestions(false)
        } else if (commandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            (commandManager as PaperCommandManager<*>).registerAsynchronousCompletions()
        }
        return commandManager
    }

    private fun registerSuggestionProviders(commandManager: PaperCommandManager<CommandSender>) {
        commandManager.parserRegistry().registerSuggestionProvider("players") { _, input ->
            CompletableFuture.completedFuture(Bukkit.getOfflinePlayers()
                .filter { p -> p.name?.startsWith(input.input(), false) ?: false }
                .map {
                    Suggestion.simple(it.name ?: it.uniqueId.toString())
                }
            )
        }
        commandManager.parserRegistry().registerSuggestionProvider("langKeys") { _, _ ->
            CompletableFuture.completedFuture(LangKey.entries.map { Suggestion.simple(it.name) })
        }
        commandManager.parserRegistry().registerSuggestionProvider("purgeKeys") { _, _ ->
            CompletableFuture.completedFuture(PurgeKey.entries.map { Suggestion.simple(it.name) })
        }
        commandManager.parserRegistry().registerSuggestionProvider("migrationKeys") { _, _ ->
            CompletableFuture.completedFuture(MigrationKey.entries.map { Suggestion.simple(it.name) })
        }
    }

    private fun createAnnotationParser(commandManager: PaperCommandManager<CommandSender>): AnnotationParser<CommandSender> {
        val annotationParser: AnnotationParser<CommandSender> = AnnotationParser(
            commandManager,
            CommandSender::class.java
        )

        return annotationParser
    }

}