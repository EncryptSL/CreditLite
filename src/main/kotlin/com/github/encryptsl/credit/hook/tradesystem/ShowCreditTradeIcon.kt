package com.github.encryptsl.credit.hook.tradesystem

import de.codingair.tradesystem.spigot.trade.gui.layout.types.TradeIcon
import de.codingair.tradesystem.spigot.trade.gui.layout.types.impl.economy.ShowEconomyIcon
import org.bukkit.inventory.ItemStack

class ShowCreditTradeIcon(itemStack: ItemStack) : ShowEconomyIcon(itemStack, "Credits") {

    override fun getOriginClass(): Class<out TradeIcon> {
        return CreditTradeIcon::class.java
    }
}