package com.github.encryptsl.credit.api.interfaces

import org.bukkit.OfflinePlayer
import java.util.UUID

interface CreditAPI {
    /**
     * Create player account to database
     * @param player is OfflinePlayer
     * @param startAmount an amount added to player when accounts is created.
     * @return Boolean
     * @see Boolean
     * @see OfflinePlayer
     */
    fun createAccount(player: OfflinePlayer, startAmount: Double): Boolean

    /**
     * Cache player account during login
     * @param uuid
     * @param amount is value of player account from database.
     * @return Boolean
     * @see Boolean
     * @see UUID
     * @see com.github.encryptsl.credit.api.economy.CreditEconomy.getBalance(uuid: UUID)
     */
    fun cacheAccount(uuid: UUID, amount: Double): Boolean

    /**
     * Delete player account from database
     * @param uuid
     * @return Boolean
     * @see UUID
     */
    fun deleteAccount(uuid: UUID): Boolean

    /**
     * Boolean for check if player have account in database
     * @param uuid
     * @return Boolean
     * @see UUID
     */
    fun hasAccount(uuid: UUID): Boolean

    /**
     * Boolean for check if player have enough money
     * @param uuid
     * @return Boolean
     * @see UUID
     */
    fun has(uuid: UUID, amount: Double): Boolean

    /**
     * Get balance of player account
     * @param uuid
     * @return Double
     * @see UUID
     */
    fun getBalance(uuid: UUID): Double

    /**
     * Deposit credits to player account
     * @param uuid
     * @param amount is amount added to player account
     * @see UUID
     */
    fun deposit(uuid: UUID, amount: Double)

    /**
     * Withdraw credits from player account
     * @param uuid
     * @param amount is amount removed from player account
     * @see UUID
     */
    fun withdraw(uuid: UUID, amount: Double)

    /**
     * Set fixed credits to player account
     * @param uuid
     * @param amount is amount fixed value
     * @see UUID
     */
    fun set(uuid: UUID, amount: Double)

    /**
     * Synchronize cache with database
     * @param uuid
     * @see UUID
     */
    fun syncAccount(uuid: UUID)

    /**
     * Synchronize all saved data in cache with database
     */
    fun syncAccounts()

    /**
     * Get top player accounts
     * @return MutableMap
     */
    fun getTopBalance(): Map<String, Double>

}