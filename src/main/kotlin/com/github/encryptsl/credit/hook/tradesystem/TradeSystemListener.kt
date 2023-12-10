package com.github.encryptsl.credit.hook.tradesystem

import com.github.encryptsl.credit.CreditLite
import de.codingair.codingapi.tools.items.ItemBuilder
import de.codingair.codingapi.tools.items.XMaterial
import de.codingair.tradesystem.spigot.events.TradeIconInitializeEvent
import de.codingair.tradesystem.spigot.trade.gui.layout.registration.EditorInfo
import de.codingair.tradesystem.spigot.trade.gui.layout.registration.TransitionTargetEditorInfo
import de.codingair.tradesystem.spigot.trade.gui.layout.registration.Type
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class TradeSystemListener(private val creditLite: CreditLite) : Listener {

    @EventHandler
    fun onInit(event: TradeIconInitializeEvent) {
        event.registerIcon(
            creditLite,
            CreditTradeIcon::class.java,
            EditorInfo("Credit icon",
                Type.ECONOMY,
                { ItemBuilder(XMaterial.EMERALD.parseItem()) },
                false,
                "CreditLite"
            )
        )

        event.registerIcon(
            creditLite,
            ShowCreditTradeIcon::class.java,
            TransitionTargetEditorInfo(
                "Credit preview icon",
                CreditTradeIcon::class.java
            )
        )
    }

}