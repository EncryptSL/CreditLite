package com.github.encryptsl.credit.common.config

import com.github.encryptsl.credit.api.enums.LangKey
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException

class Locales(private val creditLite: com.github.encryptsl.credit.CreditLite, private val langVersion: String) {

    private var langYML: FileConfiguration? = null

    fun getMessage(value: String): String {
        val key = langYML?.getString(value) ?:
        langYML?.getString("messages.admin.translation_missing")?.replace("<key>", value)
        val prefix = creditLite.config.getString("plugin.prefix")

        return key?.replace("<prefix>", prefix ?: "") ?: "Translation missing error: $value"
    }

    fun getList(value: String): MutableList<*>? {
        val list = langYML?.getList(value)?.toMutableList()
        val prefix = creditLite.config.getString("plugin.prefix")
        list?.replaceAll { it?.toString()?.replace("<prefix>", prefix ?: "") }

        return list
    }

    fun setTranslationFile(langKey: LangKey) {
        val fileName = "message_${langKey.name.lowercase()}.yml"
        val file = File("${creditLite.dataFolder}/locale/", fileName)

        try {
            if (!file.exists()) {
                file.parentFile.mkdirs()
                creditLite.saveResource("locale/$fileName", false)
            } else {
                val existingVersion = YamlConfiguration.loadConfiguration(file).getString("version")

                if (existingVersion.isNullOrEmpty() || existingVersion != langVersion) {
                    val backupFile = File(creditLite.dataFolder, "locale/old_$fileName")
                    file.copyTo(backupFile, true)
                    creditLite.saveResource("locale/$fileName", true)
                }
            }
            creditLite.config["plugin.translation"] = langKey.name
            creditLite.saveConfig()
            creditLite.reloadConfig()
            creditLite.logger.info("Loaded translation $fileName [!]")
        } catch (e: IOException) {
            creditLite.logger.warning("Unsupported language, lang file for $langKey doesn't exist [!]")
            return
        }
        langYML = YamlConfiguration.loadConfiguration(file)
    }

    fun reloadTranslation() {
        val currentLocale: String = creditLite.config.getString("plugin.translation") ?: return
        LangKey.entries.find { it.name.equals(currentLocale, ignoreCase = true) }?.let {
            setTranslationFile(it)
        }
    }
}