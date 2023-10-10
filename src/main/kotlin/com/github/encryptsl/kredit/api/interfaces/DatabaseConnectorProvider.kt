package com.github.encryptsl.kredit.api.interfaces

interface DatabaseConnectorProvider {
    fun initConnect(jdbcHost: String, user: String, pass: String)
}