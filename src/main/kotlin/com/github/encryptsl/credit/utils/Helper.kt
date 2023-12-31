package com.github.encryptsl.credit.utils

import com.github.encryptsl.credit.api.enums.CheckLevel
import com.github.encryptsl.credit.api.objects.ModernText
import com.github.encryptsl.credit.extensions.isApproachingZero
import com.github.encryptsl.credit.extensions.isNegative
import com.github.encryptsl.credit.extensions.toValidDecimal
import org.bukkit.command.CommandSender

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
}