package com.github.encryptsl.credit

import com.github.encryptsl.credit.api.ConfigAPI
import com.github.encryptsl.credit.api.economy.CreditEconomy
import com.github.encryptsl.credit.api.economy.CreditEconomyFormatting
import com.github.encryptsl.credit.api.interfaces.CreditAPI
import com.github.encryptsl.credit.common.CommandManager
import com.github.encryptsl.credit.common.config.Locales
import com.github.encryptsl.credit.common.database.DatabaseConnector
import com.github.encryptsl.credit.common.database.models.CreditModel
import com.github.encryptsl.credit.common.database.models.monolog.MonologModel
import com.github.encryptsl.credit.common.hook.HookManager
import com.github.encryptsl.credit.listeners.*
import com.github.encryptsl.credit.listeners.admin.*
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import kotlin.system.measureTimeMillis

class CreditLite : JavaPlugin() {
    companion object {
        const val CONFIG_VERSION = "1.0.1"
        const val LANG_VERSION = "2.0.1"
        const val PAPI_VERSION = "1.0.0"
    }

    val pluginManager: PluginManager = server.pluginManager

    val creditEconomyFormatting by lazy { CreditEconomyFormatting(config) }
    val locale: Locales by lazy { Locales(this, LANG_VERSION) }
    val creditModel: CreditModel by lazy { CreditModel() }
    val monologModel: MonologModel by lazy { MonologModel(this) }

    private val configAPI: ConfigAPI by lazy { ConfigAPI(this) }
    private val hookManager: HookManager by lazy { HookManager(this) }
    private val commandManager: CommandManager by lazy { CommandManager(this) }
    private val database: DatabaseConnector by lazy { DatabaseConnector(this) }

    override fun onLoad() {
        configAPI
            .create("database.db")
            .createConfig("config.yml", CONFIG_VERSION)
        locale.loadCurrentTranslation()
        database.createConnection(
            config.getString("database.connection.jdbc_url") ?: "jdbc:sqlite:plugins/LiteEco/database.db",
            config.getString("database.connection.username") ?: "root",
            config.getString("database.connection.password") ?: "admin"
        )
    }

    override fun onEnable() {
        val timeTaken = measureTimeMillis {
            blockPlugins()
            hookRegistration()
            commandManager.registerCommands()
            registerListeners()
        }
        logger.info("Plugin enabled in time $timeTaken ms")
        server.servicesManager.register(CreditAPI::class.java, CreditEconomy, this, ServicePriority.Highest)
    }

    override fun onDisable() {
        CreditEconomy.syncAccounts()
        logger.info("Plugin is disabled")
    }

    private fun blockPlugins() {}

    private fun hookRegistration() {
        hookManager.hookPAPI()
        hookManager.hookMiniPlaceholders()
        hookManager.hookTradeSystem()
    }

    private fun registerListeners() {
        var amount: Int
        val timeTaken = measureTimeMillis {
            val listeners = arrayListOf(
                AccountManageListener(this),
                PlayerCreditPayListener(this),
                GlobalCreditDepositListener(this),
                GlobalCreditSetListener(this),
                GlobalCreditWithdrawListener(this),
                CreditDepositListener(this),
                CreditWithdrawListener(this),
                CreditSetListener(this),
                PlayerJoinListener(this),
                PlayerLoginListener(this),
                PlayerQuitListener(this)
            )
            for (listener in listeners) {
                pluginManager.registerEvents(listener, this)
                logger.info("Bukkit Listener ${listener.javaClass.simpleName} registered () -> ok")
            }
            amount = listeners.size
        }
        logger.info("Listeners registered ($amount) in time $timeTaken ms -> ok")
    }
}