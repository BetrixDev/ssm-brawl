package dev.betrix.superSmashMobsBrawl.minigames.instances.interfaces

import org.bukkit.entity.Player

/**
 * Some minigame may have a feature to allow players who left the server to rejoin the minigame.
 * This interface can help with that
 */
interface OnPlayerJoin {
    fun onPlayerJoin(player: Player): Unit
}