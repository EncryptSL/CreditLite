package com.github.encryptsl.credit.api.interfaces

import java.math.BigDecimal
import java.util.*

interface AccountAPI {
    fun cacheAccount(uuid: UUID, value: BigDecimal)

    fun syncAccount(uuid: UUID)

    fun syncAccounts()

    fun removeAccount(uuid: UUID)

    fun getBalance(uuid: UUID): BigDecimal

    fun isAccountCached(uuid: UUID): Boolean

    fun isPlayerOnline(uuid: UUID): Boolean
}