package dev.betrix.superSmashMobsBrawl.utils

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.Vector

fun createLocation(world: World, vector: Vector): Location {
    return Location(world, vector.x, vector.y, vector.z)
}