package dev.betrix.superSmashMobsBrawl.extensions

import dev.betrix.superSmashMobsBrawl.passives.definitions.PassiveDefinition
import dev.betrix.superSmashMobsBrawl.services.DebugService
import dev.betrix.superSmashMobsBrawl.services.KitService
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

fun Player.hasPassive(definition: PassiveDefinition): Boolean {
    // New data-driven kits
    val brawlKit = KitService.getBrawlKit(this)
    if (brawlKit != null) {
        return brawlKit.passives.any { it.id == definition.id }
    }

    // Legacy kits
    val kit = KitService.getKitInstance(this)
    return kit?.passiveInstances?.find { it.definition.id == definition.id } != null
}
