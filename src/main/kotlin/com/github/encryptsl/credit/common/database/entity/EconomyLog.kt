package com.github.encryptsl.credit.common.database.entity

import kotlinx.datetime.Instant


data class EconomyLog(val level: String, val log: String, val timestamp: Instant)