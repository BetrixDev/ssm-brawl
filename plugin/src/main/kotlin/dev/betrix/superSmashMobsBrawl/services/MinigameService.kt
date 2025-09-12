package dev.betrix.superSmashMobsBrawl.services

import com.github.michaelbull.result.*
import com.github.shynixn.mccoroutine.bukkit.launch
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.events.QueuePopEvent
import dev.betrix.superSmashMobsBrawl.minigames.BrawlMinigameOld
import dev.betrix.superSmashMobsBrawl.minigames.PrototypingMinigameOld
import dev.betrix.superSmashMobsBrawl.minigames.TeamBasedStocksMinigameOld
import dev.betrix.superSmashMobsBrawl.models.MinigamePlayer
import dev.betrix.superSmashMobsBrawl.models.MinigameTeam
import dev.betrix.superSmashMobsBrawl.models.brawlData.FfaMinigameDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.TeamBasedStocksMinigameDef
import dev.betrix.superSmashMobsBrawl.utils.resultRunCatching
import gg.flyte.twilight.event.event
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

class MinigameService : KoinComponent {
    private val plugin: SuperSmashMobsBrawl by inject()
    private val dataService: DataService by inject()
    private val hubService: HubService by inject()

    private val inFlightMinigames = arrayListOf<BrawlMinigameOld<*>>()

    init {
        event<QueuePopEvent> {
            val minigameDef = dataService.getMinigame(minigameId) ?: return@event

            val gameId = UUID.randomUUID().toString()

            val minigameInstance =
                when (minigameDef) {
                    is TeamBasedStocksMinigameDef -> {
                        val playersPerTeam = minigameDef.playersPerTeam
                        val amountOfTeams = minigameDef.amountOfTeams

                        val teams: List<MinigameTeam> =
                            (0 until amountOfTeams).map { teamIndex ->
                                val startIndex = teamIndex * playersPerTeam
                                val endIndex = startIndex + playersPerTeam
                                val teamPlayers =
                                    players.subList(startIndex, endIndex).toMutableList()
                                // Initial stocks value will be set during minigame init from
                                // definition
                                MinigameTeam(teamPlayers, minigameDef.stocks)
                            }

                        TeamBasedStocksMinigameOld(minigameDef.id, gameId, teams)
                    }

                    is FfaMinigameDef -> {
                        when (minigameDef.id) {
                            "prototyping" -> {
                                PrototypingMinigameOld(
                                    minigameDef.id,
                                    gameId,
                                    players.map { MinigamePlayer(it) },
                                )
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

        // Handle player disconnect
        event<PlayerQuitEvent> {
            val minigameInstance = getMinigameForPlayer(player) ?: return@event

            plugin.launch { minigameInstance.onPlayerDisconnect(player) }
        }

        // Handle player reconnect
        event<PlayerJoinEvent> {
            // Check if this player was in any active minigame when they disconnected
            for (minigameInstance in inFlightMinigames) {
                if (minigameInstance.onPlayerReconnect(player)) {
                    // Player was successfully teleported back to a minigame
                    break
                }
            }
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

    private fun handleMinigameSetup(minigameInstance: BrawlMinigameOld<*>) {
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

    fun removeMinigameInstance(minigameInstance: BrawlMinigameOld<*>): Boolean {
        return inFlightMinigames.remove(minigameInstance)
    }

    fun isPlayerInMinigame(player: Player): Boolean {
        return getMinigameForPlayer(player) != null
    }

    fun getMinigameForPlayer(player: Player): BrawlMinigameOld<*>? {
        return inFlightMinigames.find { it.isPlayerInMinigame(player) }
    }

    fun handlePlayerLeave(player: Player): Result<BrawlMinigameOld<*>, MinigameLeaveError> {
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
