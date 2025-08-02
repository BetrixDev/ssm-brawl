package dev.betrix.superSmashMobsBrawl.extensions

import dev.betrix.superSmashMobsBrawl.maps.SpawnPoint
import kotlin.math.roundToInt

/** Returns `n` equally spaced spawn points */
fun List<SpawnPoint>.getEquidistant(n: Int): List<SpawnPoint> {
    if (isEmpty() || n <= 0) {
        return emptyList()
    }

    if (n == 1) {
        return listOf(this[0])
    }

    if (n >= size) {
        return this.toList()
    }

    val result = mutableListOf<SpawnPoint>()
    val step = (size - 1).toDouble() / (n - 1)

    for (i in 0 until n) {
        val index = (i * step).roundToInt()
        result.add(this[index])
    }

    return result
}
