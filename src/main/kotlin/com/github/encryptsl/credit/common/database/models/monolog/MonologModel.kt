package com.github.encryptsl.credit.common.database.models.monolog

import org.bukkit.plugin.Plugin
import java.util.logging.Level

class MonologModel(private val plugin: Plugin) : AbstractMonoLog(plugin.config.getBoolean("economy.monolog_activity", true)) {
    override fun error(message: String) {
        log(Level.SEVERE, message)
    }

    override fun warning(message: String) {
        log(Level.WARNING, message)
    }

    override fun info(message: String) {
        log(Level.INFO, message)
    }
}