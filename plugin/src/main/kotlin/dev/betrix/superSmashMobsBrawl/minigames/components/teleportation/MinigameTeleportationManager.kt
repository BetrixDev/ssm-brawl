package dev.betrix.superSmashMobsBrawl.minigames.components.teleportation

import dev.betrix.superSmashMobsBrawl.minigames.components.MinigameComponent
import org.bukkit.entity.Player

abstract class MinigameTeleportationManager : MinigameComponent() {
    abstract fun handlePlayerTeleport(player: Player)

    abstract fun handlePlayerSpectatorTeleport(player: Player)
}