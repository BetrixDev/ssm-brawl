package dev.betrix.superSmashMobsBrawl.minigames.managers

import dev.betrix.superSmashMobsBrawl.Manageable
import org.bukkit.GameMode
import org.bukkit.OfflinePlayer

interface ISpectatorManager {
    fun setSpectator(player: OfflinePlayer)

    fun clearSpectator(player: OfflinePlayer)
}

class DefaultSpectatorManager : Manageable(), ISpectatorManager {
    override fun setSpectator(player: OfflinePlayer) {
        if (player.isOnline) {
            val bukkit = player.player!!
            bukkit.gameMode = GameMode.SPECTATOR
            bukkit.allowFlight = true
            bukkit.isFlying = true
            bukkit.fallDistance = 0f
        }
    }

    override fun clearSpectator(player: OfflinePlayer) {
        if (player.isOnline) {
            val bukkit = player.player!!
            bukkit.gameMode = GameMode.SURVIVAL
        }
    }
}
