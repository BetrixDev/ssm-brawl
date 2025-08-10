package dev.betrix.superSmashMobsBrawl.utils

import dev.betrix.superSmashMobsBrawl.models.SpawnPoint
import org.bukkit.Location
import org.bukkit.World

fun createLocation(world: World, spawnPoint: SpawnPoint): Location {
    return Location(
        world,
        spawnPoint.x,
        spawnPoint.y,
        spawnPoint.z,
        spawnPoint.yaw ?: 0f,
        spawnPoint.pitch ?: 90f,
    )
}
