package com.github.encryptsl.credit.api.interfaces

import java.util.*
import java.util.concurrent.CompletableFuture

interface DatabaseSQLProvider {
    fun createPlayerAccount(username: String, uuid: UUID, credit: Double)
    fun deletePlayerAccount(uuid: UUID)
    fun getExistPlayerAccount(uuid: UUID): CompletableFuture<Boolean>
    fun getTopBalance(top: Int): MutableMap<String, Double>
    fun getTopBalance(): MutableMap<String, Double>
    fun getBalance(uuid: UUID): CompletableFuture<Double>
    fun depositCredit(uuid: UUID, credit: Double)
    fun withdrawCredit(uuid: UUID, credit: Double)
    fun setCredit(uuid: UUID, credit: Double)
    fun purgeAccounts()
    fun purgeDefaultAccounts(defaultCredit: Double)
    fun purgeInvalidAccounts()
}