package com.github.encryptsl.credit.common.hook.tradesystem

import com.github.encryptsl.credit.api.economy.CreditEconomy
import de.codingair.tradesystem.spigot.extras.external.EconomySupportType
import de.codingair.tradesystem.spigot.extras.external.TypeCap
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.EconomyIcon
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.math.BigDecimal

class CreditTradeIcon(itemStack: ItemStack) : EconomyIcon<ShowCreditTradeIcon>(itemStack, "Credit", "Credits", false) {

    override fun getTargetClass(): Class<ShowCreditTradeIcon> {
        return ShowCreditTradeIcon::class.java
    }

    override fun getBalance(player: Player): BigDecimal {
        return CreditEconomy.getBalance(player)
    }

    override fun withdraw(player: Player, value: BigDecimal) {
        CreditEconomy.withdraw(player, value)
    }

    override fun deposit(player: Player, value: BigDecimal) {
        CreditEconomy.deposit(player, value)
    }

    override fun getMaxSupportedValue(): TypeCap {
        return EconomySupportType.DOUBLE
    }
}