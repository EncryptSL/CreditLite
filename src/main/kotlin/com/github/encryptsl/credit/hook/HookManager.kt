package com.github.encryptsl.credit.hook

import com.github.encryptsl.credit.hook.placeholderapi.KreditPlaceholderAPI

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
            creditLite.logger.info("###################################")
            creditLite.logger.info("#       PlaceholderAPI Found      #")
            creditLite.logger.info("#   You can now use placeholders  #")
            creditLite.logger.info("###################################")
            KreditPlaceholderAPI(creditLite, com.github.encryptsl.credit.CreditLite.PAPI_VERSION).register()
        } else {
            creditLite.logger.info("###################################")
            creditLite.logger.info("#     PlaceholderAPI not Found    #")
            creditLite.logger.info("###################################")
        }
    }
}