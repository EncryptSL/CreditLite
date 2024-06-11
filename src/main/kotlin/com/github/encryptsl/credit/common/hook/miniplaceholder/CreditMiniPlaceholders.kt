package com.github.encryptsl.credit.common.hook.miniplaceholder

import com.github.encryptsl.credit.CreditLite
import com.github.encryptsl.credit.api.economy.CreditEconomy
import io.github.miniplaceholders.kotlin.asInsertingTag
import io.github.miniplaceholders.kotlin.expansion
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.util.*

class CreditMiniPlaceholders(private val creditLite: CreditLite) {

    fun register() {
        val expansion = expansion("credits") {
            audiencePlaceholder("balance") { a, _, _ ->
                val player: OfflinePlayer = a as OfflinePlayer
                return@audiencePlaceholder Component.text(CreditEconomy.getBalance(player)).asInsertingTag()
            }
            audiencePlaceholder("balance_formatted") { a, _, _ ->
                val player: OfflinePlayer = a as OfflinePlayer
                return@audiencePlaceholder Component
                    .text(creditLite.creditEconomyFormatting
                        .formatted(CreditEconomy.getBalance(player))
                    ).asInsertingTag()
            }
            audiencePlaceholder("balance_full_formatted") { a, _, _ ->
                val player: OfflinePlayer = a as OfflinePlayer
                return@audiencePlaceholder Component
                    .text(creditLite.creditEconomyFormatting
                        .fullFormatting(CreditEconomy.getBalance(player))
                    ).asInsertingTag()
            }
            audiencePlaceholder("balance_compacted") { a, _, _ ->
                val player: OfflinePlayer = a as OfflinePlayer
                return@audiencePlaceholder Component
                    .text(creditLite.creditEconomyFormatting
                        .compacted(CreditEconomy.getBalance(player))
                    ).asInsertingTag()
            }
            globalPlaceholder("top_rank_player") { _, _ ->
                return@globalPlaceholder Component.text(nameByRank(1)).asInsertingTag()
            }
            globalPlaceholder("top_formatted") { i, _ ->
                return@globalPlaceholder Component.text(creditLite.creditEconomyFormatting.formatted(balanceByRank(i.popOr("You need provide position.").value().toInt()))).asInsertingTag()
            }
            globalPlaceholder("top_full_formatted") { i, _ ->
                return@globalPlaceholder Component.text(creditLite.creditEconomyFormatting.fullFormatting(balanceByRank(i.popOr("You need provide position.").value().toInt()))).asInsertingTag()
            }
            globalPlaceholder("top_compacted") { i, _ ->
                return@globalPlaceholder Component.text(creditLite.creditEconomyFormatting.compacted(balanceByRank(i.popOr("You need provide position.").value().toInt()))).asInsertingTag()
            }
            globalPlaceholder("top_balance") { i, _ ->
                return@globalPlaceholder Component.text(balanceByRank(i.popOr("You need provide position.").value().toInt())).asInsertingTag()
            }
            globalPlaceholder("top_player") { i, _ ->
                return@globalPlaceholder Component.text(nameByRank(i.popOr("You need provide position.").value().toInt())).asInsertingTag()
            }
        }
        expansion.register()
    }

    private fun nameByRank(rank: Int): String {
        val topBalance = topBalance()
        return if (rank in 1..topBalance.size) {
            val playerUuid = topBalance.keys.elementAt(rank - 1)
            Bukkit.getOfflinePlayer(UUID.fromString(playerUuid)).name ?: "UNKNOWN"
        } else {
            "N/A"
        }
    }

    private fun balanceByRank(rank: Int): Double {
        val topBalance = topBalance()
        return if (rank in 1..topBalance.size) {
            topBalance.values.elementAt(rank - 1)
        } else {
            0.0
        }
    }

    private fun topBalance(): Map<String, Double> {
        return CreditEconomy.getTopBalance()
    }

}