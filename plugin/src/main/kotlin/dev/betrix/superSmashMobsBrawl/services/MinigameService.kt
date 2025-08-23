package dev.betrix.superSmashMobsBrawl.services

import com.github.michaelbull.result.*
import com.github.shynixn.mccoroutine.bukkit.launch
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.events.QueuePopEvent
import dev.betrix.superSmashMobsBrawl.minigames.BrawlMinigame
import dev.betrix.superSmashMobsBrawl.minigames.PrototypingMinigame
import dev.betrix.superSmashMobsBrawl.minigames.TeamBasedStocksMinigame
import dev.betrix.superSmashMobsBrawl.models.MinigamePlayer
import dev.betrix.superSmashMobsBrawl.models.MinigameTeam
import dev.betrix.superSmashMobsBrawl.models.brawlData.FfaMinigameDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.TeamBasedStocksMinigameDef
import dev.betrix.superSmashMobsBrawl.utils.resultRunCatching
import gg.flyte.twilight.event.event
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

sealed class MinigameInitError {
    data class PlayerAlreadyInMinigame(val players: List<Player>) : MinigameInitError()
}

enum class MinigameLeaveError {
    PlayerNotInMinigame,
    NotAllowedToLeave,
    HubNotReady,
    Unknown,
}

class MinigameService : KoinComponent {
    private val plugin: SuperSmashMobsBrawl by inject()
    private val dataService: DataService by inject()
    private val hubService: HubService by inject()

    private val inFlightMinigames = arrayListOf<BrawlMinigame<*>>()

    init {
        event<QueuePopEvent> {
            val minigameDef = dataService.getMinigame(minigameId) ?: return@event

            val gameId = UUID.randomUUID().toString()

            val minigameInstance = when (minigameDef) {
                is TeamBasedStocksMinigameDef -> {
                    val playersPerTeam = minigameDef.playersPerTeam
                    val amountOfTeams = minigameDef.amountOfTeams

                    val teams: List<MinigameTeam> =
                        (0 until amountOfTeams).map { teamIndex ->
                            val startIndex = teamIndex * playersPerTeam
                            val endIndex = startIndex + playersPerTeam
                            val teamPlayers = players.subList(startIndex, endIndex).toMutableList()
                            // Initial stocks value will be set during minigame init from definition
                            MinigameTeam(teamPlayers, minigameDef.stocks)
                        }

                    TeamBasedStocksMinigame(minigameDef.id, gameId, teams)
                }

                is FfaMinigameDef -> {
                    when (minigameDef.id) {
                        "prototyping" -> {
                            PrototypingMinigame(minigameDef.id, gameId, players.map { MinigamePlayer(it) })
                        }

                        else -> {
                            // No-op for unknown ids for now
                            TODO("handle this")
                        }
                    }
                }
            }

            handleMinigameSetup(minigameInstance)
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

    private fun handleMinigameSetup(minigameInstance: BrawlMinigame<*>) {
        plugin.launch {
            minigameInstance
                .initMinigame()
                .onSuccess {
                    if (!inFlightMinigames.contains(minigameInstance)) {
                        inFlightMinigames.add(minigameInstance)
                    }
                }
                .onFailure { err ->
                    plugin.logger.warning("Minigame init failed: $err")
                    minigameInstance.teardown()
                }
        }
    }

    fun removeMinigameInstance(minigameInstance: BrawlMinigame<*>): Boolean {
        return inFlightMinigames.remove(minigameInstance)
    }

    fun isPlayerInMinigame(player: Player): Boolean {
        return getMinigameForPlayer(player) != null
    }

    fun getMinigameForPlayer(player: Player): BrawlMinigame<*>? {
        return inFlightMinigames.find { it.isPlayerInMinigame(player) }
    }

    fun handlePlayerLeave(player: Player): Result<BrawlMinigame<*>, MinigameLeaveError> {
        val minigameInstance =
            getMinigameForPlayer(player) ?: return Err(MinigameLeaveError.PlayerNotInMinigame)

        val canPlayerLeave = minigameInstance.canPlayerLeaveMinigame(player)

        if (!canPlayerLeave) {
            return Err(MinigameLeaveError.NotAllowedToLeave)
        }

        resultRunCatching { minigameInstance.onPlayerLeave(player) }
            .onFailure { err ->
                plugin.logger.severe("Error calling minigame.onPlayerLeave $err")
                return Err(MinigameLeaveError.NotAllowedToLeave)
            }

        hubService.teleportToDefaultHub(player).onFailure {
            return Err(MinigameLeaveError.HubNotReady)
        }

        return Ok(minigameInstance)
    }
}
