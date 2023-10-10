package com.github.encryptsl.credit.common

import cloud.commandframework.annotations.AnnotationParser
import cloud.commandframework.arguments.parser.ParserParameters
import cloud.commandframework.arguments.parser.StandardParameters
import cloud.commandframework.bukkit.CloudBukkitCapabilities
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator
import cloud.commandframework.meta.CommandMeta
import cloud.commandframework.paper.PaperCommandManager
import com.github.encryptsl.credit.CreditLite
import com.github.encryptsl.credit.api.enums.LangKey
import com.github.encryptsl.credit.api.enums.MigrationKey
import com.github.encryptsl.credit.api.enums.PurgeKey
import com.github.encryptsl.credit.commands.KreditAdminCMD
import com.github.encryptsl.credit.commands.KreditCMD
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import java.util.function.Function

class CommandManager(private val creditLite: CreditLite) {

    fun registerCommands() {
        creditLite.logger.info("Registering commands with Cloud Command Framework !")

        val commandManager = createCommandManager()

        registerSuggestionProviders(commandManager)

        val annotationParser = createAnnotationParser(commandManager)
        annotationParser.parse(KreditCMD(creditLite))
        annotationParser.parse(KreditAdminCMD(creditLite))
    }

    private fun createCommandManager(): PaperCommandManager<CommandSender> {
        val executionCoordinatorFunction = AsynchronousCommandExecutionCoordinator.builder<CommandSender>().build()
        val mapperFunction = Function.identity<CommandSender>()
        val commandManager = PaperCommandManager(
            creditLite,
            executionCoordinatorFunction,
            mapperFunction,
            mapperFunction
        )
        if (commandManager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
            commandManager.registerBrigadier()
            commandManager.brigadierManager()?.setNativeNumberSuggestions(false)
        }
        if (commandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            (commandManager as PaperCommandManager<*>).registerAsynchronousCompletions()
        }
        return commandManager
    }

    private fun registerSuggestionProviders(commandManager: PaperCommandManager<CommandSender>) {
        commandManager.parserRegistry().registerSuggestionProvider("players") { _, input ->
            Bukkit.getOfflinePlayers().toList()
                .filter { p ->
                    p.name?.startsWith(input) ?: false
                }
                .mapNotNull { it.name }
        }
        commandManager.parserRegistry().registerSuggestionProvider("langKeys") { _, _ ->
            LangKey.entries.map { key -> key.name }.toList()
        }
        commandManager.parserRegistry().registerSuggestionProvider("purgeKeys") { _, _ ->
            PurgeKey.entries.map { key -> key.name }.toList()
        }
        commandManager.parserRegistry().registerSuggestionProvider("migrationKeys") { _, _ ->
            MigrationKey.entries.map { key -> key.name }.toList()
        }
    }

    private fun createAnnotationParser(commandManager: PaperCommandManager<CommandSender>): AnnotationParser<CommandSender> {
        val commandMetaFunction = Function<ParserParameters, CommandMeta> { p: ParserParameters ->
            CommandMeta.simple() // Decorate commands with descriptions
                .with(CommandMeta.DESCRIPTION, p[StandardParameters.DESCRIPTION, "No Description"])
                .build()
        }
        return AnnotationParser(
            commandManager,
            CommandSender::class.java,
            commandMetaFunction /* Mapper for command meta instances */
        )
    }

}