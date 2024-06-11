package com.github.encryptsl.credit.common.database.entity

import java.util.UUID

data class User(val userName: String, val uuid: UUID, val money: Double)