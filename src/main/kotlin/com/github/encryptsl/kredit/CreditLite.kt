package com.github.encryptsl.kredit

import com.github.encryptsl.kredit.api.ConfigAPI
import com.github.encryptsl.kredit.api.economy.CreditEconomy
import com.github.encryptsl.kredit.common.CommandManager
import com.github.encryptsl.kredit.config.Locales
import com.github.encryptsl.kredit.database.DatabaseConnector
import com.github.encryptsl.kredit.database.models.CreditModel
import com.github.encryptsl.kredit.hook.HookManager
import com.github.encryptsl.kredit.listeners.*
import org.bstats.bukkit.Metrics
import org.bstats.charts.SingleLineChart
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin
import kotlin.system.measureTimeMillis

class CreditLite : JavaPlugin() {
    companion object {
        const val CONFIG_VERSION = "1.0.0"
        const val LANG_VERSION = "1.0.0"
        const val PAPI_VERSION = "1.0.0"
    }

    val pluginManager: PluginManager = server.pluginManager

    var countTransactions: LinkedHashMap<String, Int> = LinkedHashMap()

    val api: CreditEconomy by lazy { CreditEconomy(this) }
    val locale: Locales by lazy { Locales(this, LANG_VERSION) }
    val creditModel: CreditModel by lazy { CreditModel() }

    private val configAPI: ConfigAPI by lazy { ConfigAPI(this) }
    private val hookManager: HookManager by lazy { HookManager(this) }
    private val commandManager: CommandManager by lazy { CommandManager(this) }

    override fun onLoad() {
        configAPI
            .create("database.db")
            .createConfig("config.yml", CONFIG_VERSION)
        locale
            .reloadTranslation()
        DatabaseConnector()
            .initConnect(
                config.getString("database.connection.jdbc_url") ?: "jdbc:sqlite:plugins/LiteEco/database.db",
                config.getString("database.connection.username") ?: "root",
                config.getString("database.connection.password") ?: "admin"
            )
    }

    override fun onEnable() {
        val timeTaken = measureTimeMillis {
            blockPlugins()
            hookRegistration()
            setupMetrics()
            commandManager.registerCommands()
            registerListeners()
        }
        logger.info("Plugin enabled in time $timeTaken ms")
    }

    override fun onDisable() {
        api.syncAccounts()
        logger.info("Plugin is disabled")
    }

    private fun blockPlugins() {
        hookManager.blockPlugin("Treasury")
        hookManager.blockPlugin("Towny")
    }

    private fun hookRegistration() {
        hookManager.hookPAPI()
    }

    private fun setupMetrics() {
        val metrics = Metrics(this, 15144)
        metrics.addCustomChart(SingleLineChart("transactions") {
            countTransactions["transactions"]
        })
    }

    private fun registerListeners() {
        var amount: Int
        val timeTaken = measureTimeMillis {
            val listeners = arrayListOf(
                AccountEconomyManageListener(this),
                PlayerEconomyPayListener(this),
                AdminEconomyGlobalDepositListener(this),
                AdminEconomyGlobalSetListener(this),
                AdminEconomyGlobalWithdrawListener(this),
                AdminEconomyMoneyDepositListener(this),
                AdminEconomyMoneyWithdrawListener(this),
                AdminEconomyMoneySetListener(this),
                PlayerJoinListener(this),
                PlayerQuitListener(this)
            )
            listeners.forEach { listener -> pluginManager.registerEvents(listener, this)
                logger.info("Bukkit Listener ${listener.javaClass.simpleName} registered () -> ok")
            }
            amount = listeners.size
        }
        logger.info("Listeners registered ($amount) in time $timeTaken ms -> ok")
    }
}