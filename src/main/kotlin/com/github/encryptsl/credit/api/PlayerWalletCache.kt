package com.github.encryptsl.credit.api

import com.github.encryptsl.credit.api.interfaces.AccountAPI
import com.github.encryptsl.credit.common.database.models.CreditModel
import org.bukkit.Bukkit
import java.util.*

class PlayerWalletCache : AccountAPI {

    private val creditModel: CreditModel by lazy { CreditModel() }
    private val cache: HashMap<UUID, Double> = HashMap()

    override fun cacheAccount(uuid: UUID, value: Double) {
        if (!isAccountCached(uuid)) {
            cache[uuid] = value
        } else {
            cache[uuid] = value
        }
    }

    override fun getBalance(uuid: UUID): Double {
        return cache.getOrDefault(uuid, 0.0)
    }

    override fun syncAccount(uuid: UUID) {
        try {
            creditModel.setCredit(uuid, getBalance(uuid))
            removeAccount(uuid)
        } catch (_ : Exception) {}
    }

    override fun syncAccounts() {
        try {
            val cache = cache
            if (cache.isEmpty()) return
            for (p in cache) {
                creditModel.setCredit(p.key, p.value)
            }
            cache.clear()
        } catch (_: Exception) {}
    }

    override fun removeAccount(uuid: UUID) {
        cache.remove(uuid)
    }

    override fun isAccountCached(uuid: UUID): Boolean {
        return cache.containsKey(uuid)
    }

    override fun isPlayerOnline(uuid: UUID): Boolean {
        return Bukkit.getPlayer(uuid) != null
    }
}