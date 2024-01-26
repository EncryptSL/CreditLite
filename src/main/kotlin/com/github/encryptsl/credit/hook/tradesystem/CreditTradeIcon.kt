package com.github.encryptsl.credit.hook.tradesystem

import com.github.encryptsl.credit.CreditLite
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
        return CreditLite().getAPI().getBalance(player).toBigDecimal()
    }

    override fun withdraw(player: Player, value: BigDecimal) {
        CreditLite().getAPI().withdraw(player, value.toDouble())
    }

    override fun deposit(player: Player, value: BigDecimal) {
        CreditLite().getAPI().deposit(player, value.toDouble())
    }

    override fun getMaxSupportedValue(): TypeCap {
        return EconomySupportType.DOUBLE
    }
}