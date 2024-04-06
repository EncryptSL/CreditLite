package com.github.encryptsl.credit.api.interfaces

import com.github.encryptsl.credit.common.database.entity.EconomyLog

interface AdapterLogger {
    fun error(message: String)
    fun warning(message: String)
    fun info(message: String)
    fun clearLogs()
    fun getLog(): List<EconomyLog>
}