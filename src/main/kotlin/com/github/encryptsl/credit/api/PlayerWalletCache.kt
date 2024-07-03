package com.github.encryptsl.credit.api

import com.github.encryptsl.credit.api.interfaces.AccountAPI
import com.github.encryptsl.credit.common.database.models.CreditModel
import org.bukkit.Bukkit
import java.math.BigDecimal
import java.util.*

object PlayerWalletCache : AccountAPI {

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

    override fun syncAccount(uuid: UUID, value: BigDecimal) {
        try {
            creditModel.setCredit(uuid, value)
        } catch (_ : Exception) {}
    }

    override fun syncAccount(uuid: UUID) {
        try {
            creditModel.setCredit(uuid, getBalance(uuid))
            clearFromCache(uuid)
        } catch (_ : Exception) {}
    }

    override fun syncAccounts() {
        try {
            val iterator = cache.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()
                syncAccount(next.key, next.value)
            }
            cache.clear()
        } catch (_: Exception) {}
    }

    override fun clearFromCache(uuid: UUID) {
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