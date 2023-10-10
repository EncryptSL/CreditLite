package com.github.encryptsl.credit.api.interfaces

interface DatabaseConnectorProvider {
    fun initConnect(jdbcHost: String, user: String, pass: String)
}