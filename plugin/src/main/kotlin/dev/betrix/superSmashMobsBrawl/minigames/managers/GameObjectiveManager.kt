package dev.betrix.superSmashMobsBrawl.minigames.managers

import dev.betrix.superSmashMobsBrawl.IManageable
import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.events.BrawlDeathEvent
import dev.betrix.superSmashMobsBrawl.minigames.BrawlMinigame
import dev.betrix.superSmashMobsBrawl.minigames.MinigameTeam
import org.bukkit.GameMode
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

sealed class WinResult {
    object None : WinResult()

    data class Winners(val winners: List<OfflinePlayer>) : WinResult()

    data class WinningTeam(val team: MinigameTeam) : WinResult()
}

interface IGameObjectiveManager : IManageable {
    fun recordDeath(player: OfflinePlayer)

    fun checkWinCondition(): WinResult
}

class DefaultGameObjectiveManager(private val minigame: BrawlMinigame) :
    Manageable(), IGameObjectiveManager {
    override fun recordDeath(player: OfflinePlayer) {}

    override fun checkWinCondition(): WinResult = WinResult.None
}

/** FFA minigame objective manager - last player standing wins */
class FfaGameObjectiveManager(private val minigame: BrawlMinigame) :
    Manageable(), IGameObjectiveManager {

    override fun setup() {
        // Listen for death events to check win conditions
        listeners.add(
            BrawlDeathEvent.listen {
                if (isPlayerInMinigame(player)) {
                    recordDeath(player)
                }
            }
        )
    }

    override fun recordDeath(player: OfflinePlayer) {
        // In FFA, we just need to check win conditions after each death
        val winResult = checkWinCondition()
        when (winResult) {
            is WinResult.Winners -> {
                minigame.endMinigame("winner_determined", winResult.winners)
            }
            else -> {} // Continue playing
        }
    }

    override fun checkWinCondition(): WinResult {
        // Count active players (not spectator mode, still connected, and not disconnected)
        val activePlayers =
            minigame.allPlayers().filter { player ->
                val bukkitPlayer = player.player
                bukkitPlayer != null &&
                    bukkitPlayer.isOnline &&
                    !minigame.connectionManager.isDisconnected(player) &&
                    bukkitPlayer.gameMode != GameMode.SPECTATOR &&
                    !minigame.respawnManager.isRespawning(player)
            }

        // For FFA, end game if 1 or fewer players remain
        return when {
            activePlayers.isEmpty() -> WinResult.None // No winners
            activePlayers.size == 1 -> WinResult.Winners(activePlayers)
            else -> WinResult.None // Continue playing
        }
    }

    private fun isPlayerInMinigame(player: Player): Boolean {
        return minigame.allPlayers().any { it.isOnline && it.player?.uniqueId == player.uniqueId }
    }
}

/** Team-based stocks minigame objective manager - teams have lives/stocks */
class TeamBasedStocksObjectiveManager(private val minigame: BrawlMinigame) :
    Manageable(), IGameObjectiveManager {
    override fun setup() {
        // Initialize team stocks from minigame definition
        val stocksAmount =
            minigame.minigameDef.let { def ->
                if (
                    def
                        is
                        dev.betrix.superSmashMobsBrawl.models.brawlData.TeamBasedStocksMinigameDef
                ) {
                    def.stocks
                } else {
                    3 // Default stocks
                }
            }

        minigame.teams.forEach { team -> team.metadata["stocks"] = stocksAmount }

        // Listen for death events to reduce stocks
        listeners.add(
            BrawlDeathEvent.listen {
                if (isPlayerInMinigame(player)) {
                    recordDeath(player)
                }
            }
        )
    }

    override fun recordDeath(player: OfflinePlayer) {
        val team = minigame.teamManager.findTeamOf(player) ?: return
        val currentStocks = team.metadata["stocks"] as? Int ?: return

        if (currentStocks > 0) {
            team.metadata["stocks"] = currentStocks - 1

            // Check win conditions after reducing stocks
            val winResult = checkWinCondition()
            when (winResult) {
                is WinResult.WinningTeam -> {
                    minigame.endMinigame("team_victory", winResult.team.players)
                }
                else -> {} // Continue playing
            }
        }
    }

    override fun checkWinCondition(): WinResult {
        val teamsWithStocks =
            minigame.teams.filter { team ->
                val stocks = team.metadata["stocks"] as? Int ?: 0
                stocks > 0
            }

        return when {
            teamsWithStocks.isEmpty() -> WinResult.None // No winners
            teamsWithStocks.size == 1 -> WinResult.WinningTeam(teamsWithStocks.first())
            else -> WinResult.None // Continue playing
        }
    }

    private fun isPlayerInMinigame(player: Player): Boolean {
        return minigame.allPlayers().any { it.isOnline && it.player?.uniqueId == player.uniqueId }
    }
}

/** Parkour minigame objective manager - first to finish wins */
class ParkourObjectiveManager(private val minigame: BrawlMinigame) :
    Manageable(), IGameObjectiveManager {
    private val finishedPlayers = mutableSetOf<java.util.UUID>()

    override fun setup() {
        // Future: Listen for checkpoint/finish line events
    }

    override fun recordDeath(player: OfflinePlayer) {
        // In parkour, death might reset progress or teleport back to start
        // This would be handled by respawn manager
    }

    override fun checkWinCondition(): WinResult {
        // Future implementation: Check if someone finished the parkour course
        return WinResult.None
    }

    fun recordFinish(player: Player) {
        if (finishedPlayers.add(player.uniqueId)) {
            // First player to finish wins
            minigame.endMinigame("parkour_completed", listOf(player))
        }
    }
}
