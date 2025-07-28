package dev.betrix.superSmashMobsBrawl.utils

import dev.betrix.superSmashMobsBrawl.services.DebugService
import org.bukkit.entity.Player

fun isOnGround(player: Player): Boolean {
    return player.location.subtract(0.0, 0.5, 0.0).block.type.isSolid
}

/** 
 * Check if debug mode is enabled for this player.
 * 
 * Usage:
 * ```kotlin
 * if (player.hasDebugEnabled()) {
 *     player.sendMessage("Debug: Some debug information")
 * }
 * ```
 */
fun Player.hasDebugEnabled(): Boolean {
    return DebugService.isDebugEnabled(this)
}
