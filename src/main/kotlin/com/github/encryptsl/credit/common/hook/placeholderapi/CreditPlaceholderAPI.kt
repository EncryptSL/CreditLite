package com.github.encryptsl.credit.common.hook.placeholderapi

import com.github.encryptsl.credit.api.economy.CreditEconomy
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer
import java.math.BigDecimal
import java.util.*

class CreditPlaceholderAPI(private val creditLite: com.github.encryptsl.credit.CreditLite, private val extVersion: String) : PlaceholderExpansion() {

    override fun getIdentifier(): String = "credit"

    override fun getAuthor(): String = "EncryptSL"

    override fun getVersion(): String = extVersion

    override fun getRequiredPlugin(): String = creditLite.name

    override fun persist(): Boolean = true

    override fun canRegister(): Boolean {
        return creditLite.pluginManager.getPlugin(requiredPlugin)!!.isEnabled
    }

    override fun onRequest(player: OfflinePlayer?, identifier: String): String? {
        if (player == null) return null
        val args = identifier.split("_")
        val rank = args.getOrNull(2)?.toIntOrNull()

        return when (identifier) {
            "balance" -> creditLite.creditEconomyFormatting.formatted(CreditEconomy.getBalance(player))
            "balance_formatted" -> creditLite.creditEconomyFormatting.fullFormatting(CreditEconomy.getBalance(player))
            "balance_compacted" -> creditLite.creditEconomyFormatting.compacted(CreditEconomy.getBalance(player))
            "top_rank_player" -> nameByRank(1)
            else -> rank?.let {
                when {
                    identifier.startsWith("top_formatted_") -> creditLite.creditEconomyFormatting.fullFormatting(balanceByRank(rank))
                    identifier.startsWith("top_compacted_") -> creditLite.creditEconomyFormatting.compacted(balanceByRank(rank))
                    identifier.startsWith("top_balance_") -> creditLite.creditEconomyFormatting.formatted(balanceByRank(rank))
                    identifier.startsWith("top_player_") -> nameByRank(rank)
                    else -> null
                }
            }
        }
    }

    private fun nameByRank(rank: Int): String {
        val topBalance = topBalance()
        return if (rank in 1..topBalance.size) {
            Optional.ofNullable(topBalance.keys.elementAt(rank - 1)).orElse("UNKNOWN")
        } else {
            "N/A"
        }
    }

    private fun balanceByRank(rank: Int): BigDecimal {
        val topBalance = topBalance()
        return if (rank in 1..topBalance.size) {
            topBalance.values.elementAt(rank - 1)
        } else {
            BigDecimal.ZERO
        }
    }

    private fun topBalance(): Map<String, BigDecimal> {
        return CreditEconomy.getTopBalance()
    }
}
