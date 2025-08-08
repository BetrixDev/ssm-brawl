package dev.betrix.superSmashMobsBrawl.extensions

import dev.betrix.superSmashMobsBrawl.maps.SpawnPoint as MapSpawnPoint
import dev.betrix.superSmashMobsBrawl.models.SpawnPoint as ModelSpawnPoint
import kotlin.jvm.JvmName
import kotlin.math.roundToInt

/** Returns `n` equally spaced spawn points for model-based spawn points */
@JvmName("getEquidistantModel")
fun List<ModelSpawnPoint>.getEquidistant(n: Int): List<ModelSpawnPoint> {
    if (isEmpty() || n <= 0) return emptyList()
    if (n == 1) return listOf(this[0])
    if (n >= size) return this.toList()

    val result = mutableListOf<ModelSpawnPoint>()
    val step = (size - 1).toDouble() / (n - 1)
    for (i in 0 until n) {
        val index = (i * step).roundToInt()
        result.add(this[index])
    }
    return result
}

/** Returns `n` equally spaced spawn points for builder-based map spawn points */
@JvmName("getEquidistantMap")
fun List<MapSpawnPoint>.getEquidistant(n: Int): List<MapSpawnPoint> {
    if (isEmpty() || n <= 0) return emptyList()
    if (n == 1) return listOf(this[0])
    if (n >= size) return this.toList()

    val result = mutableListOf<MapSpawnPoint>()
    val step = (size - 1).toDouble() / (n - 1)
    for (i in 0 until n) {
        val index = (i * step).roundToInt()
        result.add(this[index])
    }
    return result
}
