package com.github.encryptsl.credit.api

import com.github.encryptsl.credit.api.interfaces.AccountAPI
import com.github.encryptsl.credit.database.models.CreditModel
import com.github.encryptsl.credit.api.objects.AccountCache
import com.github.encryptsl.credit.utils.DebugLogger
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.logging.Level

class PlayerAccount(val plugin: Plugin) : AccountAPI {

    private val creditModel: CreditModel by lazy { CreditModel() }
    val debugLogger: DebugLogger by lazy { DebugLogger(plugin) }

    override fun cacheAccount(uuid: UUID, value: Double) {
        if (!isAccountCached(uuid)) {
            AccountCache.cache[uuid] = value
            debugLogger.debug(Level.INFO, "Account $uuid with $value was changed successfully !")
        } else {
            AccountCache.cache[uuid] = value
            debugLogger.debug(Level.INFO, "Account $uuid with $value was changed successfully  !")
        }
    }

    override fun getBalance(uuid: UUID): Double {
        return AccountCache.cache.getOrDefault(uuid, 0.0)
    }

    override fun syncAccount(uuid: UUID) {
        runCatching {
            creditModel.setCredit(uuid, getBalance(uuid))
        }.onSuccess {
            debugLogger.debug(Level.INFO,"Account $uuid was synced with database  !")
            removeAccount(uuid)
        }.onFailure {
            debugLogger.debug(Level.SEVERE,it.message ?: it.localizedMessage)
        }
    }

    override fun syncAccounts() {
        runCatching {
            AccountCache.cache.toList().forEach { k ->
                creditModel.setCredit(k.first, k.second)
            }
        }.onSuccess {
            debugLogger.debug(Level.INFO,"Accounts are synced with database !")
            AccountCache.cache.clear()
        }.onFailure {
            debugLogger.debug(Level.SEVERE,it.message ?: it.localizedMessage)
        }
    }

    override fun removeAccount(uuid: UUID) {
        AccountCache.cache.remove(uuid)
    }

    override fun isAccountCached(uuid: UUID): Boolean {
        return AccountCache.cache.containsKey(uuid)
    }

    override fun isPlayerOnline(uuid: UUID): Boolean {
        return Bukkit.getPlayer(uuid) != null
    }
}