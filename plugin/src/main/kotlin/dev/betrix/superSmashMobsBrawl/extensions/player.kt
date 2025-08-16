package dev.betrix.superSmashMobsBrawl.extensions

import dev.betrix.superSmashMobsBrawl.services.DebugService
import dev.betrix.superSmashMobsBrawl.services.KitService
import dev.betrix.superSmashMobsBrawl.services.LangService
import org.bukkit.Location
import org.bukkit.entity.Player
import org.koin.core.context.GlobalContext

/**
 * Check if debug mode is enabled for this player.
 *
 * Usage:
 * ```kotlin
 * if (player.hasDebugEnabled()) {
 *     player.sendMessage("Some debug information")
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

    val lang: LangService = GlobalContext.get().get()

    sendMessage(lang.t("messages.debug") { "message" to message })
}

fun Player.hasPassive(passiveId: String): Boolean {
    val kitService: KitService = GlobalContext.get().get()

    val brawlKit = kitService.getKitForPlayer(this) ?: return false

    return brawlKit.getPassive(passiveId) != null
}

/**
 * Gets a location in front of the player's eyes.
 *
 * @param distance How far to look ahead (max distance if raycasting)
 */
fun Player.getLocationInFrontOfEyes(distance: Double): Location {
    val world = this.world
    val eyeLocation = this.eyeLocation
    val direction = eyeLocation.direction.normalize()

    // Perform a ray trace from the player's eye
    val result = world.rayTraceBlocks(eyeLocation, direction, distance)

    return if (result != null && result.hitBlock != null) {
        // Obstacle found: return location just before it
        val hitPos = result.hitPosition
        val safeLocation = hitPos.toLocation(world).subtract(direction.multiply(0.5))
        safeLocation.y += 0.1 // lift slightly to avoid clipping
        safeLocation.setDirection(direction)
    } else {
        // No obstacle: return full distance location
        val target = eyeLocation.add(direction.multiply(distance))
        target.setDirection(direction)
    }
}
