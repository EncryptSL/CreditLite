package com.github.encryptsl.credit.common.extensions

import java.math.BigDecimal

fun BigDecimal.isNegative(): Boolean {
    return this < BigDecimal.ZERO
}

fun BigDecimal.isZero(): Boolean {
    return this == BigDecimal.ZERO
}

fun BigDecimal.isApproachingZero(): Boolean {
    return this < BigDecimal.valueOf(0.001)
}

fun String.isDecimal(): Boolean {
    return toDoubleOrNull()?.takeIf { it.isFinite() } != null
}
fun String.toDecimal(): BigDecimal? {
    return toDoubleOrNull()?.takeIf { it.isFinite() }?.toBigDecimal()
}