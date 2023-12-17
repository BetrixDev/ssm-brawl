package dev.betrix.supersmashmobsbrawl.extensions

fun String.isDouble(): Boolean {
    return try {
        this.toDouble()
        true
    } catch (e: NumberFormatException) {
        false
    }
}