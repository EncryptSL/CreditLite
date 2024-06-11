package com.github.encryptsl.credit.api.interfaces

import com.github.encryptsl.credit.common.database.entity.User
import java.util.*
import java.util.concurrent.CompletableFuture

interface CreditDataSourceSQL {
    fun createPlayerAccount(username: String, uuid: UUID, credit: Double)
    fun deletePlayerAccount(uuid: UUID)
    fun getExistPlayerAccount(uuid: UUID): CompletableFuture<Boolean>
    fun getTopBalance(): MutableMap<String, Double>
    fun getUserByUUID(uuid: UUID): CompletableFuture<User>
    fun depositCredit(uuid: UUID, credit: Double)
    fun withdrawCredit(uuid: UUID, credit: Double)
    fun setCredit(uuid: UUID, credit: Double)
    fun purgeAccounts()
    fun purgeDefaultAccounts(defaultCredit: Double)
    fun purgeInvalidAccounts()
}