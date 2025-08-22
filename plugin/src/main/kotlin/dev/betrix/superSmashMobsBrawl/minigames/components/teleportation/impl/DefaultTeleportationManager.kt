package dev.betrix.superSmashMobsBrawl.minigames.components.teleportation.impl

import dev.betrix.superSmashMobsBrawl.extensions.location
import dev.betrix.superSmashMobsBrawl.minigames.components.teleportation.MinigameTeleportationManager
import org.bukkit.entity.Player

class DefaultTeleportationManager : MinigameTeleportationManager() {
    override fun handlePlayerTeleport(player: Player) {
        TODO("Not yet implemented")
    }

    override fun handlePlayerSpectatorTeleport(player: Player) {
        val spectatorSpawnPoint = minigame.brawlWorld.data.spectatorSpawnPoint

        player.teleport(minigame.brawlWorld.world.location(spectatorSpawnPoint))
    }

}