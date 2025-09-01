package dev.betrix.superSmashMobsBrawl.extensions

import dev.betrix.superSmashMobsBrawl.models.SpawnPoint
import kotlin.math.roundToInt
import org.bukkit.World
import org.bukkit.entity.Player

/** Returns `n` equally spaced spawn points for model-based spawn points */
fun List<SpawnPoint>.getEquidistant(n: Int): List<SpawnPoint> {
    if (isEmpty() || n <= 0) return emptyList()
    if (n == 1) return listOf(this[0])
    if (n >= size) return this.toList()

    val result = mutableListOf<SpawnPoint>()
    val step = (size - 1).toDouble() / (n - 1)
    for (i in 0 until n) {
        val index = (i * step).roundToInt()
        result.add(this[index])
    }
    return result
}

/**
 * Returns the spawn point that maximizes the minimum squared distance to the provided players in
 * the given world. If no players are in the specified world, returns the first spawn point, or null
 * if the list is empty.
 */
fun List<SpawnPoint>.getFarthestFromPlayers(
    players: Collection<Player>,
    world: World,
): SpawnPoint? {
    if (isEmpty()) return null

    val relevantPlayers = players.filter { it.world.uid == world.uid }
    if (relevantPlayers.isEmpty()) return first()

    var bestSpawnPoint: SpawnPoint = this[0]
    var bestScore = Double.NEGATIVE_INFINITY

    for (spawn in this) {
        val sx = spawn.x
        val sy = spawn.y
        val sz = spawn.z

        var minDistanceSquaredToPlayers = Double.POSITIVE_INFINITY
        for (player in relevantPlayers) {
            val loc = player.location
            val dx = loc.x - sx
            val dy = loc.y - sy
            val dz = loc.z - sz
            val distanceSquared = dx * dx + dy * dy + dz * dz
            if (distanceSquared < minDistanceSquaredToPlayers) {
                minDistanceSquaredToPlayers = distanceSquared
            }
        }

        if (minDistanceSquaredToPlayers > bestScore) {
            bestScore = minDistanceSquaredToPlayers
            bestSpawnPoint = spawn
        }
    }

    return bestSpawnPoint
}

fun <T> List<T>.hasSameElements(other: List<T>): Boolean {
    if (this.size != other.size) return false

    val counts = mutableMapOf<T, Int>()

    // Count elements in first list
    for (element in this) {
        counts[element] = counts.getOrDefault(element, 0) + 1
    }

    // Subtract counts based on second list
    for (element in other) {
        val count = counts[element] ?: return false
        if (count == 1) {
            counts.remove(element)
        } else {
            counts[element] = count - 1
        }
    }

    return counts.isEmpty()
}