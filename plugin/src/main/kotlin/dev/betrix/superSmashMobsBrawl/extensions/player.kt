package dev.betrix.superSmashMobsBrawl.extensions

import dev.betrix.superSmashMobsBrawl.services.DebugService
import dev.betrix.superSmashMobsBrawl.utils.mm
import org.bukkit.entity.Player

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

fun Player.sendDebugMessage(message: String) {
    if (!hasDebugEnabled()) {
        return
    }

    sendMessage(mm("<gray>[DEBUG] $message</gray>"))
}
