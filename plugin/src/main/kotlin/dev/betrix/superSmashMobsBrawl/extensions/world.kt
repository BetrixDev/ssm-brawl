package dev.betrix.superSmashMobsBrawl.extensions

import dev.betrix.superSmashMobsBrawl.models.SpawnPoint
import org.bukkit.Location
import org.bukkit.World

fun World.location(spawnPoint: SpawnPoint): Location {
    return Location(
        this,
        spawnPoint.x,
        spawnPoint.y,
        spawnPoint.z,
        spawnPoint.yaw ?: 0f,
        spawnPoint.pitch ?: 90f,
    )
}
