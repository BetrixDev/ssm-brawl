package dev.betrix.superSmashMobsBrawl.extensions

import dev.betrix.superSmashMobsBrawl.disguises.Disguise
import org.bukkit.entity.Player

/**
 * Extension function to easily get a player's active disguise
 *
 * @return The player's active disguise, or null if none exists
 */
fun Player.getDisguise(): Disguise? {
    return Disguise.getDisguise(this)
}
