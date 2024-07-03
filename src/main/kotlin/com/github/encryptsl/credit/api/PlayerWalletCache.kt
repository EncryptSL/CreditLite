package com.github.encryptsl.credit.api

import com.github.encryptsl.credit.api.interfaces.AccountAPI
import com.github.encryptsl.credit.common.database.models.CreditModel
import org.bukkit.Bukkit
import java.math.BigDecimal
import java.util.*

class PlayerWalletCache : AccountAPI {

    private val creditModel: CreditModel by lazy { CreditModel() }
    private val cache: HashMap<UUID, BigDecimal> = HashMap()

    override fun cacheAccount(uuid: UUID, value: BigDecimal) {
        if (!isAccountCached(uuid)) {
            cache[uuid] = value
        } else {
            cache[uuid] = value
        }
    }

    override fun getBalance(uuid: UUID): BigDecimal {
        return cache.getOrDefault(uuid, BigDecimal.ZERO)
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