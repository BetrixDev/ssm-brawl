package dev.betrix.superSmashMobsBrawl.extensions

import dev.betrix.superSmashMobsBrawl.disguises.BrawlDisguise
import org.bukkit.entity.Player

/**
 * Extension to easily get a player's active disguise
 *
 * @return The player's active disguise, or null if none exists
 */
val Player.disguise: BrawlDisguise?
    get() = BrawlDisguise.getDisguise(this)
