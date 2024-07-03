package com.github.encryptsl.credit.api.interfaces

import com.github.encryptsl.credit.common.database.entity.User
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.CompletableFuture

interface CreditDataSourceSQL {
    fun createPlayerAccount(username: String, uuid: UUID, credit: BigDecimal)
    fun deletePlayerAccount(uuid: UUID)
    fun getExistPlayerAccount(uuid: UUID): CompletableFuture<Boolean>
    fun getTopBalance(): MutableMap<String, BigDecimal>
    fun getUserByUUID(uuid: UUID): CompletableFuture<User>
    fun depositCredit(uuid: UUID, credit: BigDecimal)
    fun withdrawCredit(uuid: UUID, credit: BigDecimal)
    fun setCredit(uuid: UUID, credit: BigDecimal)
    fun purgeAccounts()
    fun purgeDefaultAccounts(defaultCredit: BigDecimal)
    fun purgeInvalidAccounts()
}