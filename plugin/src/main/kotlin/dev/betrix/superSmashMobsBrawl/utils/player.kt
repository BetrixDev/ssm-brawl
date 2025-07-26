package dev.betrix.superSmashMobsBrawl.utils

import org.bukkit.entity.Player

fun isOnGround(player: Player): Boolean {
    return player.location.subtract(0.0, 0.5, 0.0).block.type.isSolid
}
