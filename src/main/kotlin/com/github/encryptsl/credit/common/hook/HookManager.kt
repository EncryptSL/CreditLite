package com.github.encryptsl.credit.common.hook

import com.github.encryptsl.credit.CreditLite
import com.github.encryptsl.credit.common.hook.placeholderapi.CreditPlaceholderAPI
import com.github.encryptsl.credit.common.hook.tradesystem.TradeSystemListener

class HookManager(private val creditLite: com.github.encryptsl.credit.CreditLite) {

    /**
     * Method for disable plugin if is detected unsupported plugin.
     * @param pluginName - String name of plugin is CaseSensitive.
     */
    fun blockPlugin(pluginName: String) {
        if (creditLite.pluginManager.isPluginEnabled(pluginName)) {
            creditLite.logger.severe("Please don't use $pluginName, because there can be conflict.")
            creditLite.pluginManager.disablePlugin(creditLite)
        }
    }

    /**
     * Method for check if plugin is installed
     * @param pluginName - String name of plugin is CaseSensitive
     * @return Boolean
     */
    private fun isPluginInstalled(pluginName: String): Boolean {
        return creditLite.pluginManager.getPlugin(pluginName) != null
    }

    /**
     * Method of registering Placeholders if plugin PlaceholderAPI is enabled.
     */
    fun hookPAPI() {
        if (isPluginInstalled("PlaceholderAPI")) {
            creditLite.logger.info("PlaceholderAPI found, hook successfully")
            CreditPlaceholderAPI(creditLite, CreditLite.PAPI_VERSION).register()
        } else {
            creditLite.logger.info("PlaceholderAPI not found, placeholders not working !")
        }
    }

    fun hookTradeSystem() {
        if (isPluginInstalled("TradeSystem")) {
            creditLite.logger.info("TradeSystem found, hook successfully")
            creditLite.pluginManager.registerEvents(TradeSystemListener(creditLite), creditLite)
        } else {
            creditLite.logger.info("TradeSystem not found, credit trading not working !")
        }
    }

}