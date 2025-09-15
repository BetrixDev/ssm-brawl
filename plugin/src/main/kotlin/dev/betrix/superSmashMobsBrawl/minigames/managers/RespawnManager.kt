package dev.betrix.superSmashMobsBrawl.minigames.managers

import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.minigames.BrawlMinigame
import org.bukkit.OfflinePlayer

interface IRespawnManager {
    fun markRespawning(player: OfflinePlayer)
    fun clearRespawning(player: OfflinePlayer)
    fun isRespawning(player: OfflinePlayer): Boolean
}

class DefaultRespawnManager(private val minigame: BrawlMinigame) : Manageable(), IRespawnManager {
    private val respawning = mutableSetOf<java.util.UUID>()

    override fun markRespawning(player: OfflinePlayer) {
        respawning.add(player.uniqueId)
    }

    override fun clearRespawning(player: OfflinePlayer) {
        respawning.remove(player.uniqueId)
    }

    override fun isRespawning(player: OfflinePlayer): Boolean {
        return respawning.contains(player.uniqueId)
    }
}

