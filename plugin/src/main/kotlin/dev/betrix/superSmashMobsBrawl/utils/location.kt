package dev.betrix.superSmashMobsBrawl.utils

import dev.betrix.superSmashMobsBrawl.maps.SpawnPoint
import org.bukkit.Location
import org.bukkit.World

fun createLocation(world: World, spawnPoint: SpawnPoint): Location {
    if (spawnPoint.yaw == null || spawnPoint.pitch == null) {
        return Location(world, spawnPoint.position.x, spawnPoint.position.y, spawnPoint.position.z)
    }

    return Location(
        world,
        spawnPoint.position.x,
        spawnPoint.position.y,
        spawnPoint.position.z,
        spawnPoint.yaw,
        spawnPoint.pitch,
    )
}

fun createLocation(world: World, spawnPoint: dev.betrix.superSmashMobsBrawl.models.SpawnPoint): Location {
    return Location(
        world,
        spawnPoint.x,
        spawnPoint.y,
        spawnPoint.z,
        spawnPoint.yaw ?: 0f,
        spawnPoint.pitch ?: 90f,
    )
}
