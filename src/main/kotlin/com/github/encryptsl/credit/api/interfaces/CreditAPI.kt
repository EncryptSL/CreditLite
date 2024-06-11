package com.github.encryptsl.credit.api.interfaces

import com.github.encryptsl.credit.common.database.entity.User
import org.bukkit.OfflinePlayer
import java.util.UUID
import java.util.concurrent.CompletableFuture

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
     * @param player
     * @param amount is value of player account from database.
     * @see OfflinePlayer
     * @see com.github.encryptsl.credit.api.economy.CreditEconomy.getBalance(uuid: UUID)
     */
    fun cacheAccount(player: OfflinePlayer, amount: Double)

    /**
     * Delete player account from database
     * @param player
     * @return Boolean
     * @see OfflinePlayer
     */
    fun deleteAccount(player: OfflinePlayer): Boolean

    /**
     * Boolean for check if player have account in database
     * @param uuid is {@link UUID}
     * @return Boolean
     * @see UUID
     */
    fun hasAccount(uuid: UUID): CompletableFuture<Boolean>

    /**
     * Boolean for check if player have enough money
     * @param player
     * @return Boolean
     * @see player
     */
    fun has(player: OfflinePlayer, amount: Double): Boolean

    /**
     * Get user account
     * @param player is OfflinePlayer
     * @return CompletableFuture<User>
     * @see OfflinePlayer
     */
    fun getUserByUUID(player: OfflinePlayer): CompletableFuture<User>

    /**
     * Get balance of player account
     * @param player
     * @return Double
     * @see OfflinePlayer
     */
    fun getBalance(player: OfflinePlayer): Double

    /**
     * Deposit credits to player account
     * @param player
     * @param amount is amount added to player account
     * @see OfflinePlayer
     */
    fun deposit(player: OfflinePlayer, amount: Double)

    /**
     * Withdraw credits from player account
     * @param player
     * @param amount is amount removed from player account
     * @see OfflinePlayer
     */
    fun withdraw(player: OfflinePlayer, amount: Double)

    /**
     * Set fixed credits to player account
     * @param player
     * @param amount is amount fixed value
     * @see OfflinePlayer
     */
    fun set(player: OfflinePlayer, amount: Double)

    /**
     * Synchronize cache with database
     * @param player
     * @see OfflinePlayer
     */
    fun syncAccount(player: OfflinePlayer)

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