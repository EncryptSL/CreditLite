package encryptsl.cekuj.net.commands

import cloud.commandframework.annotations.*
import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.enums.TransactionType
import encryptsl.cekuj.net.api.enums.TranslationKey
import encryptsl.cekuj.net.api.events.ConsoleEconomyTransactionEvent
import encryptsl.cekuj.net.api.events.PlayerEconomyPayEvent
import encryptsl.cekuj.net.api.objects.ModernText
import encryptsl.cekuj.net.extensions.playerPosition
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import java.util.stream.Collectors.toMap

@Suppress("UNUSED")
@CommandDescription("Provided plugin by LiteEco")
class MoneyCMD(private val liteEco: LiteEco) {

    @CommandMethod("money|bal|balance [player]")
    @CommandPermission("lite.eco.money")
    fun onBalance(commandSender: CommandSender, @Argument(value = "player", suggestions = "offlinePlayers") offlinePlayer: OfflinePlayer?) {
        if (commandSender is Player) {
            if (offlinePlayer == null) {
                commandSender.sendMessage(
                    ModernText.miniModernText(
                        liteEco.translationConfig.getMessage("messages.balance_format"),
                        TagResolver.resolver(
                            Placeholder.parsed(
                                "money",
                                liteEco.econ.format(liteEco.econ.getBalance(commandSender.player)).toString()
                            )
                        )
                    )
                )
            } else {
                commandSender.sendMessage(
                    ModernText.miniModernText(
                        liteEco.translationConfig.getMessage("messages.balance_format_target"),
                        TagResolver.resolver(
                            Placeholder.parsed("target", offlinePlayer.name.toString()),
                            Placeholder.parsed("money", liteEco.econ.format(liteEco.econ.getBalance(offlinePlayer)).toString())
                        )
                    )
                )
            }
            return
        }

        if (offlinePlayer == null) {
            liteEco.translationConfig.getList("messages.help")?.forEach { s ->
                commandSender.sendMessage(ModernText.miniModernText(s.toString()))
            }
        } else {
            commandSender.sendMessage(
                ModernText.miniModernText(
                    liteEco.translationConfig.getMessage("messages.balance_format_target"),
                    TagResolver.resolver(
                        Placeholder.parsed("target", offlinePlayer.name.toString()),
                        Placeholder.parsed("money", liteEco.econ.format(liteEco.econ.getBalance(offlinePlayer)).toString())
                    )
                )
            )
        }
    }

    @ProxiedBy("baltop")
    @CommandMethod("money top")
    @CommandPermission("lite.eco.top")
    fun onTopBalance(commandSender: CommandSender) {
        val sorted = liteEco.preparedStatements.getTopBalance(10)
            .entries
            .stream()
            .sorted(compareByDescending { o1 -> o1.value })
            .collect(
                toMap({ e -> e.key }, { e -> e.value }, { _, e2 -> e2 }) { LinkedHashMap() })

        commandSender.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.balance_top_line_first")))

        sorted.playerPosition { index, entry ->
            commandSender.sendMessage(
                ModernText.miniModernText(
                    liteEco.translationConfig.getMessage("messages.balance_top_format"),
                    TagResolver.resolver(
                        Placeholder.parsed("position", index.toString()),
                        Placeholder.parsed(
                            "player",
                            Bukkit.getOfflinePlayer(UUID.fromString(entry.key)).name.toString()
                        ),
                        Placeholder.parsed("money", liteEco.econ.format(entry.value).toString())
                    )
                )
            )
        }
        commandSender.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.balance_top_line_second")))
    }

    @CommandMethod("money|bal|balance help")
    @CommandPermission("lite.eco.help")
    fun onHelp(commandSender: CommandSender) {
        liteEco.translationConfig.getList("messages.help")?.forEach { s ->
            commandSender.sendMessage(ModernText.miniModernText(s.toString()))
        }
    }

    @CommandMethod("money|bal|balance pay <player> <amount>")
    @CommandPermission("lite.eco.pay")
    fun onPayMoney(
        player: Player,
        @Argument(value = "player", suggestions = "offlinePlayers") offlinePlayer: OfflinePlayer,
        @Argument(value = "amount") amount: Double
    ) {
        if (player.name == offlinePlayer.name) {
            player.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.self_pay_error")))
            return
        }
        liteEco.server.scheduler.runTask(liteEco) { ->
            liteEco.pluginManger.callEvent(PlayerEconomyPayEvent(player, offlinePlayer, TransactionType.PAY, amount))
        }
    }

    @CommandMethod("money|bal|balance add <player> <amount>")
    @CommandPermission("lite.eco.add")
    fun onAddMoney(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "offlinePlayers") offlinePlayer: OfflinePlayer,
        @Argument(value = "amount") amount: Double
    ) {
        liteEco.server.scheduler.runTask(liteEco) { ->
            liteEco.pluginManger.callEvent(ConsoleEconomyTransactionEvent(commandSender, offlinePlayer, TransactionType.ADD, amount))
        }
    }

    @CommandMethod("money|bal|balance set <player> <amount>")
    @CommandPermission("lite.eco.set")
    fun onSetBalance(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "offlinePlayers") offlinePlayer: OfflinePlayer,
        @Argument(value = "amount") amount: Double
    ) {
        liteEco.server.scheduler.runTask(liteEco) { ->
            liteEco.pluginManger.callEvent(
                ConsoleEconomyTransactionEvent(
                    commandSender,
                    offlinePlayer,
                    TransactionType.SET,
                    amount
                )
            )
        }
    }

    @CommandMethod("money|bal|balance remove <player> <amount>")
    @CommandPermission("lite.eco.remove")
    fun onRemoveAccount(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "offlinePlayers") offlinePlayer: OfflinePlayer,
        @Argument(value = "amount") amount: Double
    ) {
        liteEco.server.scheduler.runTask(liteEco) { ->
            liteEco.pluginManger.callEvent(
                ConsoleEconomyTransactionEvent(
                    commandSender,
                    offlinePlayer,
                    TransactionType.WITHDRAW,
                    amount
                )
            )
        }
    }

    @CommandMethod("money|bal|balance lang <isoKey>")
    @CommandPermission("lite.eco.lang")
    fun onLangSwitch(
        commandSender: CommandSender,
        @Argument(value = "isoKey", suggestions = "translationKeys") translationKey: TranslationKey
    ) {
        when (translationKey) {
            TranslationKey.CS_CZ -> {
                liteEco.translationConfig.setTranslationFile(TranslationKey.CS_CZ)
                commandSender.sendMessage(
                    ModernText.miniModernText(
                        liteEco.translationConfig.getMessage("messages.translation_switch"),
                        TagResolver.resolver(Placeholder.parsed("locale", translationKey.name))
                    )
                )
            }

            TranslationKey.EN_US -> {
                liteEco.translationConfig.setTranslationFile(TranslationKey.EN_US)
                commandSender.sendMessage(
                    ModernText.miniModernText(
                        liteEco.translationConfig.getMessage("messages.translation_switch"),
                        TagResolver.resolver(Placeholder.parsed("locale", translationKey.name))
                    )
                )
            }
        }
    }

    @CommandMethod("money|bal|balance reload")
    @CommandPermission("lite.eco.reload")
    fun onReload(commandSender: CommandSender) {
        liteEco.reloadConfig()
        commandSender.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.config_reload")))
        liteEco.logger.info("Config.yml was reloaded !")
        liteEco.saveConfig()
        liteEco.translationConfig.reloadTranslationConfig()
    }
}