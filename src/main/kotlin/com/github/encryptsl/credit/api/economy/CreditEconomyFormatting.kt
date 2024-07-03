package com.github.encryptsl.credit.api.economy

import com.github.encryptsl.credit.common.extensions.compactFormat
import com.github.encryptsl.credit.common.extensions.moneyFormat
import org.bukkit.configuration.file.FileConfiguration
import java.math.BigDecimal

class CreditEconomyFormatting(private val configuration: FileConfiguration) {

    fun compacted(amount: BigDecimal): String {
        return amount.compactFormat(configuration.getString("formatting.currency_pattern").toString(), configuration.getString("formatting.compacted_pattern").toString(), configuration.getString("formatting.currency_locale").toString())
    }

    fun formatted(amount: BigDecimal): String {
        return amount.moneyFormat(configuration.getString("formatting.currency_pattern").toString(), configuration.getString("formatting.currency_locale").toString())
    }

    fun fullFormatting(amount: BigDecimal): String {
        val value = if (configuration.getBoolean("economy.compact_display")) {
            compacted(amount)
        }
        else {
            formatted(amount)
        }
        return configuration.getString("economy.currency_format").toString()
            .replace("<credits>", value)
            .replace("{credits}", value)
            .replace("%credits%", value)
    }

}