package com.github.encryptsl.credit.utils

import com.github.encryptsl.credit.api.economy.CreditEconomy
import com.github.encryptsl.credit.api.enums.CheckLevel
import com.github.encryptsl.credit.api.objects.ModernText
import com.github.encryptsl.credit.extensions.isApproachingZero
import com.github.encryptsl.credit.extensions.isNegative
import com.github.encryptsl.credit.extensions.positionIndexed
import com.github.encryptsl.credit.extensions.toValidDecimal
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
                commandSender.sendMessage(ModernText.miniModernText(creditLite.locale.getMessage("messages.error.format_amount")))
                null
            }
            checkLevel == CheckLevel.ONLY_NEGATIVE && amount.isNegative() || checkLevel == CheckLevel.FULL && (amount.isApproachingZero()) -> {
                commandSender.sendMessage(ModernText.miniModernText(creditLite.locale.getMessage("messages.error.negative_amount")))
                null
            }
            else -> amount
        }
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