package com.github.encryptsl.credit.api.interfaces

import com.github.encryptsl.credit.common.database.entity.EconomyLog
import java.util.concurrent.CompletableFuture

interface AdapterLogger {
    fun error(message: String)
    fun warning(message: String)
    fun info(message: String)
    fun clearLogs()
    fun getLog(): CompletableFuture<List<EconomyLog>>
}