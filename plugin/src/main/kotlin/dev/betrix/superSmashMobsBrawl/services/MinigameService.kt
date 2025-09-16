package dev.betrix.superSmashMobsBrawl.services

import com.github.michaelbull.result.*
import com.github.shynixn.mccoroutine.bukkit.launch
import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.events.QueuePopEvent
import dev.betrix.superSmashMobsBrawl.minigames.BrawlMinigame
import dev.betrix.superSmashMobsBrawl.minigames.MinigameState
import dev.betrix.superSmashMobsBrawl.minigames.MinigameTeam
import dev.betrix.superSmashMobsBrawl.models.brawlData.FfaMinigameDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.TeamBasedStocksMinigameDef
import dev.betrix.superSmashMobsBrawl.utils.resultRunCatching
import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.repeatingTask
import java.util.UUID
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

sealed class MinigameInitError {
    data class PlayerAlreadyInMinigame(val players: List<Player>) : MinigameInitError()
}

enum class MinigameLeaveError {
    PlayerNotInMinigame,
    NotAllowedToLeave,
    HubNotReady,
    Unknown,
}

class MinigameService : Manageable(), KoinComponent {
    private val plugin: SuperSmashMobsBrawl by inject()
    private val dataService: DataService by inject()
    private val hubService: HubService by inject()

    private val inFlightMinigames = arrayListOf<BrawlMinigame>()

    init {
        // Cleanup ended minigames periodically to prevent memory leaks
        runnables.add(
            repeatingTask(100) { // Every 5 seconds
                cleanupEndedMinigames()
            }
        )

        event<QueuePopEvent> {
            val minigameDef = dataService.getMinigame(minigameId) ?: return@event

            val gameId = UUID.randomUUID().toString()

            val teams =
                when (minigameDef) {
                    is TeamBasedStocksMinigameDef -> {
                        val playersPerTeam = minigameDef.playersPerTeam
                        val amountOfTeams = minigameDef.amountOfTeams

                        players.chunked(playersPerTeam).take(amountOfTeams).mapIndexed { idx, chunk ->
                            MinigameTeam(chunk, name = "Team ${idx + 1}")
                        }
                    }

                    is FfaMinigameDef -> {
                        // In FFA, each player is their own team
                        players.map { player -> MinigameTeam(listOf(player), name = player.name) }
                    }
                }

            val minigameInstance = BrawlMinigame(minigameDef, teams)
            plugin.launch { handleMinigameSetup(minigameInstance, gameId) }
        }
    }

    fun getMinigameData(id: String): MinigameDef? {
        return dataService.getMinigame(id)
    }

    fun getAllMinigameData(): List<MinigameDef> {
        return dataService.getAllMinigames()
    }

    fun findClosestMinigameById(id: String): MinigameDef? {
        if (id.isBlank()) return null

        getMinigameData(id)?.let {
            return it
        }

        getAllMinigameData()
            .find { it.id.equals(id, ignoreCase = true) }
            ?.let {
                return it
            }

        return getAllMinigameData().find { it.id.contains(id, ignoreCase = true) }
    }

    private suspend fun handleMinigameSetup(minigameInstance: BrawlMinigame, gameId: String) {
        val minigameSetupResult = resultRunCatching {
            minigameInstance.setup(gameId)
            minigameInstance.startGame()
        }
        minigameSetupResult
            .onSuccess {
                plugin.logger.info("Started minigame ${minigameInstance.minigameDef.id}")
                inFlightMinigames.add(minigameInstance)
            }
            .onFailure { err ->
                plugin.logger.severe("Failed to setup minigame: $err")
                // Teleport all players back to hub or something
                minigameInstance.allPlayers().forEach { player ->
                    if (player.isOnline) {
                        hubService.teleportToDefaultHub(player.player!!)
                    }
                }
                minigameInstance.teardown()
            }
    }

    fun removeMinigameInstance(minigameInstance: BrawlMinigame): Boolean {
        return inFlightMinigames.remove(minigameInstance)
    }

    fun getMinigameForPlayer(player: Player): BrawlMinigame? {
        return inFlightMinigames.find { minigame ->
            minigame.getState() != MinigameState.ENDED &&
                minigame.allPlayers().any {
                    it.isOnline && it.player?.uniqueId == player.uniqueId
                } &&
                !minigame.connectionManager.isDisconnected(player)
        }
    }

    private fun cleanupEndedMinigames() {
        val endedMinigames = inFlightMinigames.filter { it.getState() == MinigameState.ENDED }
        if (endedMinigames.isNotEmpty()) {
            plugin.logger.info("Cleaning up ${endedMinigames.size} ended minigames")
            inFlightMinigames.removeAll(endedMinigames)
        }
    }

    fun isPlayerInMinigame(player: Player): Boolean {
        return getMinigameForPlayer(player) != null
    }

    fun leaveMinigame(player: Player): Result<Unit, MinigameLeaveError> {
        val minigame =
            getMinigameForPlayer(player) ?: return Err(MinigameLeaveError.PlayerNotInMinigame)

        if (!minigame.connectionManager.canPlayerLeaveMinigame(player)) {
            return Err(MinigameLeaveError.NotAllowedToLeave)
        }

        hubService.teleportToDefaultHub(player).onFailure {
            return Err(MinigameLeaveError.HubNotReady)
        }

        minigame.connectionManager.handlePlayerLeave(player)

        return Ok(Unit)
    }
}
