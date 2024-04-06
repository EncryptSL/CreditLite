package com.github.encryptsl.credit.api.interfaces

import org.bukkit.OfflinePlayer

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
     * @param player is OfflinePlayer
     * @param amount is value of player account from database.
     * @return Boolean
     * @see Boolean
     * @see OfflinePlayer
     * @see com.github.encryptsl.credit.api.economy.CreditEconomy.getBalance(uuid: UUID)
     */
    fun cacheAccount(player: OfflinePlayer, amount: Double): Boolean

    /**
     * Delete player account from database
     * @param player is OfflinePlayer
     * @return Boolean
     * @see OfflinePlayer
     */
    fun deleteAccount(player: OfflinePlayer): Boolean

    /**
     * Boolean for check if player have account in database
     * @param player is {@link OfflinePlayer}
     * @return Boolean
     * @see OfflinePlayer
     */
    fun hasAccount(player: OfflinePlayer): Boolean

    /**
     * Boolean for check if player have enough money
     * @param player is OfflinePlayer
     * @return Boolean
     * @see OfflinePlayer
     */
    fun has(player: OfflinePlayer, amount: Double): Boolean

    /**
     * Get balance of player account
     * @param player is OfflinePlayer
     * @return Double
     * @see OfflinePlayer
     */
    fun getBalance(player: OfflinePlayer): Double

    /**
     * Deposit credits to player account
     * @param player is OfflinePlayer
     * @param amount is amount added to player account
     * @see OfflinePlayer
     */
    fun deposit(player: OfflinePlayer, amount: Double)

    /**
     * Withdraw credits from player account
     * @param player is OfflinePlayer
     * @param amount is amount removed from player account
     * @see OfflinePlayer
     */
    fun withdraw(player: OfflinePlayer, amount: Double)

    /**
     * Set fixed credits to player account
     * @param player is OfflinePlayer
     * @param amount is amount fixed value
     * @see OfflinePlayer
     */
    fun set(player: OfflinePlayer, amount: Double)

    /**
     * Synchronize cache with database
     * @param offlinePlayer is OfflinePlayer
     * @see OfflinePlayer
     */
    fun syncAccount(offlinePlayer: OfflinePlayer)

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