package com.github.encryptsl.credit.common.config

import com.github.encryptsl.credit.api.enums.LangKey
import com.github.encryptsl.credit.api.objects.ModernText
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class Locales(private val creditLite: com.github.encryptsl.credit.CreditLite, private val langVersion: String) {

    private var langYML: FileConfiguration? = null

    fun translation(translationKey: String)
        = ModernText.miniModernText(getMessage(translationKey))

    fun translation(translationKey: String, tagResolver: TagResolver)
        = ModernText.miniModernText(getMessage(translationKey), tagResolver)

    fun getMessage(value: String): String {
        val key = langYML?.getString(value) ?:
        langYML?.getString("messages.admin.translation_missing")?.replace("<key>", value)
        val prefix = creditLite.config.getString("plugin.prefix", "").toString()

        return key?.replace("<prefix>", prefix) ?: "Translation missing error: $value"
    }

    fun getList(value: String): MutableList<*>? {
        val list = langYML?.getList(value)?.toMutableList()
        val prefix = creditLite.config.getString("plugin.prefix", "").toString()
        list?.replaceAll { it?.toString()?.replace("<prefix>", prefix) }

        return list
    }

    fun loadLocales() {
        for (lang in LangKey.entries) {
            val fileName = "message_${lang.name.lowercase()}"
            val file = File("${creditLite.dataFolder}/locale/$fileName")
            if (file.exists()) continue
            file.parentFile.mkdirs()
            creditLite.saveResource("locale/$fileName", false)
        }
    }

    fun setLocale(langKey: LangKey) {
        val currentLocale: String = creditLite.config.getString("plugin.translation") ?: return
        val fileName = "message_${getRequiredLocaleOrFallback(langKey, currentLocale)}.yml"
        val file = File("${creditLite.dataFolder}/locale/", fileName)
        try {
            if (!file.exists()) {
                file.parentFile.mkdirs()
                creditLite.saveResource("locale/$fileName", false)
            }

            val existingVersion = YamlConfiguration.loadConfiguration(file).getString("version")
            if (existingVersion.isNullOrEmpty() || existingVersion != langVersion) {
                val backupFile = File(creditLite.dataFolder, "locale/old_$fileName")
                file.copyTo(backupFile, true)
                creditLite.saveResource("locale/$fileName", true)
            }

            creditLite.config["plugin.translation"] = langKey.name
            creditLite.saveConfig()
            creditLite.reloadConfig()
            creditLite.logger.info("Loaded translation $fileName [!]")

            langYML = YamlConfiguration.loadConfiguration(file)
        } catch (_: Exception) {
            creditLite.logger.warning("Unsupported language, lang file for $langKey doesn't exist [!]")
        }
    }

    private fun getRequiredLocaleOrFallback(langKey: LangKey, currentLocale: String): String {
        return LangKey.entries.stream().map<String>(LangKey::name).filter {el -> el.equals(langKey.name, true)}.findFirst().orElse(currentLocale)
    }

    fun loadCurrentTranslation() {
        val currentLocale: String = creditLite.config.getString("plugin.translation") ?: return
        setLocale(LangKey.valueOf(currentLocale))
    }
}