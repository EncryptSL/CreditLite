package com.github.encryptsl.kredit.api

import encryptsl.cekuj.net.api.interfaces.ConfigAPIProvider
import java.io.File

/**
 * @author EncryptSL(Patrik Kolařík)
 * This is part of Config API from ForgeCore by EncryptSL
 * This componentAPI is for Loading custom config.
 * Called static in mainMethod.
 */
class ConfigAPI(private val creditLite: com.github.encryptsl.kredit.CreditLite) : ConfigAPIProvider {
    override fun create(fileName: String) : ConfigAPI {
        val file = File(creditLite.dataFolder, fileName)
        if (!file.exists()) {
            creditLite.saveResource(fileName, false)
        } else {
            creditLite.logger.info("Resource $fileName exists [!]")
        }
        return this
    }

    override fun createConfig(configName: String, version: String): ConfigAPI {
        val file = File(creditLite.dataFolder, configName)
        if (!file.exists()) {
            creditLite.saveResource(configName, false)
            creditLite.logger.info("Configuration $configName was successfully created !")
        } else {
            val fileVersion = creditLite.config.getString("version")

            if (fileVersion.isNullOrEmpty() || fileVersion != version) {
                file.copyTo(File(creditLite.dataFolder, "old_$configName"), true)
                creditLite.saveResource(configName, true)
                creditLite.config["version"] = version
                creditLite.saveConfig()
                creditLite.logger.info("Configuration config.yml was outdated [!]")
            } else {
                creditLite.logger.info("Configuration config.yml is the latest [!]")
            }
        }
        return this
    }
}