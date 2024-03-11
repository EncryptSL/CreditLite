package com.github.encryptsl.credit.api.economy

import com.github.encryptsl.credit.extensions.compactFormat
import com.github.encryptsl.credit.extensions.moneyFormat
import org.bukkit.configuration.file.FileConfiguration

class CreditEconomyFormatting(private val configuration: FileConfiguration) {

    fun compacted(amount: Double): String {
        return amount.compactFormat(configuration.getString("formatting.currency_pattern").toString(), configuration.getString("formatting.compacted_pattern").toString(), configuration.getString("formatting.currency_locale").toString())
    }

    private fun formatted(amount: Double): String {
        return amount.moneyFormat(configuration.getString("formatting.currency_pattern").toString(), configuration.getString("formatting.currency_locale").toString())
    }

    fun fullFormatting(amount: Double): String {
        val value = if (configuration.getBoolean("plugin.economy.compact_display")) {
            compacted(amount)
        }
        else {
            formatted(amount)
        }
        return "${configuration.getString("economy.currency_prefix").toString()}${value} ${configuration.getString("economy.currency_name").toString()}"
    }

}