package com.github.encryptsl.credit.utils

import com.github.encryptsl.credit.api.economy.CreditEconomy
import com.github.encryptsl.credit.api.enums.CheckLevel
import com.github.encryptsl.credit.common.extensions.isApproachingZero
import com.github.encryptsl.credit.common.extensions.isNegative
import com.github.encryptsl.credit.common.extensions.positionIndexed
import com.github.encryptsl.credit.common.extensions.toValidDecimal
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import java.util.*

class Helper(private val creditLite: com.github.encryptsl.credit.CreditLite) {
    fun validateAmount(amountStr: String, commandSender: CommandSender, checkLevel: CheckLevel = CheckLevel.FULL): Double? {
        val amount = amountStr.toValidDecimal()
        return when {
            amount == null -> {
                commandSender.sendMessage(creditLite.locale.translation("messages.error.format_amount"))
                null
            }
            checkLevel == CheckLevel.ONLY_NEGATIVE && amount.isNegative() || checkLevel == CheckLevel.FULL && (amount.isApproachingZero()) -> {
                commandSender.sendMessage(creditLite.locale.translation("messages.error.negative_amount"))
                null
            }
            else -> amount
        }
    }

    fun getTopBalances()
        = CreditEconomy.getTopBalance().toList().sortedByDescending { e -> e.second }.positionIndexed { index, pair ->
            creditLite.locale.getMessage("messages.balance.top_format")
                .replace("<position>", index.toString())
                .replace("<player>", Bukkit.getOfflinePlayer(UUID.fromString(pair.first)).name.toString())
                .replace("<credit>", creditLite.creditEconomyFormatting.fullFormatting(pair.second))
        }

    fun getComponentBal(offlinePlayer: OfflinePlayer): TagResolver {
        return TagResolver.resolver(
            Placeholder.parsed("target", offlinePlayer.name.toString()),
            Placeholder.parsed(
                "credit",
                creditLite.creditEconomyFormatting.fullFormatting(CreditEconomy.getBalance(offlinePlayer))
            )
        )
    }

    fun getAccountsToMigrationData(): List<MigrationData?> {
        return creditLite.creditModel.getTopBalance().toList().positionIndexed { index, k ->
            Bukkit.getOfflinePlayer(UUID.fromString(k.first)).name?.let { MigrationData(index, k.first, it, k.second) }
        }
    }
}