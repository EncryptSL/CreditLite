package com.github.encryptsl.kredit.extensions

fun Double.isNegative(): Boolean {
    return this < 0
}

fun Double.isZero(): Boolean {
    return this == 0.0
}