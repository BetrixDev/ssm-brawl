package dev.betrix.supersmashmobsbrawl.utils

import org.bukkit.entity.Entity

fun isOnGround(entity: Entity): Boolean {
    return entity.location.subtract(0.0, 0.5, 0.0).block.type.isSolid
}