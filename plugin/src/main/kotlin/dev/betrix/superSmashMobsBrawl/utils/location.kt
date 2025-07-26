package dev.betrix.superSmashMobsBrawl.utils

import dev.betrix.superSmashMobsBrawl.maps.SpawnPoint
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.Vector

fun createLocation(world: World, spawnPoint: SpawnPoint): Location {
    if (spawnPoint.yaw == null || spawnPoint.pitch == null) {
        return Location(world, spawnPoint.position.x, spawnPoint.position.y, spawnPoint.position.z)
    }

    return Location(world, spawnPoint.position.x, spawnPoint.position.y, spawnPoint.position.z, spawnPoint.yaw, spawnPoint.pitch)
}