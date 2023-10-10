package com.github.encryptsl.kredit.api

import com.github.encryptsl.kredit.api.interfaces.AccountAPI
import com.github.encryptsl.kredit.database.models.CreditModel
import com.github.encryptsl.kredit.utils.DebugLogger
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.logging.Level

class PlayerAccount(val plugin: Plugin) : AccountAPI {

    private val cache: MutableMap<UUID, Double> = HashMap()
    private val creditModel: CreditModel by lazy { CreditModel() }
    val debugLogger: DebugLogger by lazy { DebugLogger(plugin) }

    override fun cacheAccount(uuid: UUID, value: Double) {
        if (!isAccountCached(uuid)) {
            cache[uuid] = value
            debugLogger.debug(Level.INFO, "Account $uuid with $value was changed successfully !")
        } else {
            cache[uuid] = value
            debugLogger.debug(Level.INFO, "Account $uuid with $value was changed successfully  !")
        }
    }

    override fun getBalance(uuid: UUID): Double {
        return cache.getOrDefault(uuid, 0.0)
    }

    override fun syncAccount(uuid: UUID) {
        runCatching {
            creditModel.setMoney(uuid, getBalance(uuid))
        }.onSuccess {
            debugLogger.debug(Level.INFO,"Account $uuid was synced with database  !")
            removeAccount(uuid)
        }.onFailure {
            debugLogger.debug(Level.SEVERE,it.message ?: it.localizedMessage)
        }
    }

    override fun syncAccounts() {
        runCatching {
            cache.toList().forEach { a ->
                creditModel.setMoney(a.first, a.second)
            }
        }.onSuccess {
            debugLogger.debug(Level.INFO,"Accounts are synced with database !")
            cache.clear()
        }.onFailure {
            debugLogger.debug(Level.SEVERE,it.message ?: it.localizedMessage)
        }
    }

    override fun removeAccount(uuid: UUID) {
        val player = cache.keys.find { key -> key == uuid } ?: return

        cache.remove(player)
    }

    override fun isAccountCached(uuid: UUID): Boolean {
        return cache.containsKey(uuid)
    }

    override fun isPlayerOnline(uuid: UUID): Boolean {
        return Bukkit.getPlayer(uuid) != null
    }
}