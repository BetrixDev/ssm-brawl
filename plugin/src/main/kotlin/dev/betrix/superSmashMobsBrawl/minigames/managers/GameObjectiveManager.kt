package dev.betrix.superSmashMobsBrawl.minigames.managers

import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.minigames.BrawlMinigame
import org.bukkit.OfflinePlayer

sealed class WinResult {
    object None : WinResult()
    data class Winners(val winners: List<OfflinePlayer>) : WinResult()
}

interface IGameObjectiveManager {
    fun initialize(minigame: BrawlMinigame)
    fun recordDeath(player: OfflinePlayer)
    fun checkWinCondition(): WinResult
}

class DefaultGameObjectiveManager : Manageable(), IGameObjectiveManager {
    override fun initialize(minigame: BrawlMinigame) {}

    override fun recordDeath(player: OfflinePlayer) {}

    override fun checkWinCondition(): WinResult = WinResult.None
}

