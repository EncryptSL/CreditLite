package com.github.encryptsl.credit.api

import com.github.encryptsl.credit.api.interfaces.AccountAPI
import com.github.encryptsl.credit.database.models.CreditModel
import com.github.encryptsl.credit.api.objects.AccountCache
import org.bukkit.Bukkit
import java.util.*

class PlayerAccount : AccountAPI {

    private val creditModel: CreditModel by lazy { CreditModel() }

    override fun cacheAccount(uuid: UUID, value: Double) {
        if (!isAccountCached(uuid)) {
            AccountCache.cache[uuid] = value
        } else {
            AccountCache.cache[uuid] = value
        }
    }

    override fun getBalance(uuid: UUID): Double {
        return AccountCache.cache.getOrDefault(uuid, 0.0)
    }

    override fun syncAccount(uuid: UUID) {
        try {
            creditModel.setCredit(uuid, getBalance(uuid))
            removeAccount(uuid)
        } catch (_ : Exception) {}
    }

    override fun syncAccounts() {
        try {
            AccountCache.cache.toList().forEach { k ->
                creditModel.setCredit(k.first, k.second)
            }
            AccountCache.cache.clear()
        } catch (_: Exception) {}
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