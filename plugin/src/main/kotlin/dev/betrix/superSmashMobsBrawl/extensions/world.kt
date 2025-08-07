package dev.betrix.superSmashMobsBrawl.extensions

import dev.betrix.superSmashMobsBrawl.models.SpawnPoint
import dev.betrix.superSmashMobsBrawl.maps.SpawnPoint as MapSpawnPoint
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

fun World.location(spawnPoint: MapSpawnPoint): Location {
    val pos = spawnPoint.position
    return Location(
        this,
        pos.x,
        pos.y,
        pos.z,
        spawnPoint.yaw ?: 0f,
        spawnPoint.pitch ?: 90f,
    )
}
