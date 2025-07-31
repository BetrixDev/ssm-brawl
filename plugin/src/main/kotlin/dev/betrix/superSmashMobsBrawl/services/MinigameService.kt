package dev.betrix.superSmashMobsBrawl.services

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.github.shynixn.mccoroutine.bukkit.launch
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import dev.betrix.superSmashMobsBrawl.minigames.instances.MinigameInstance
import dev.betrix.superSmashMobsBrawl.models.MinigameTeam
import org.bukkit.entity.Player

sealed class MinigameInitError {
    data class PlayerAlreadyInMinigame(val players: List<Player>) : MinigameInitError()
}

enum class MinigameLeaveError {
    PlayerNotInMinigame,
    NotAllowedToLeave,
    HubNotReady,
    Unknown,
}

object MinigameService {
    private val inFlightMinigames = arrayListOf<MinigameInstance>()

    fun initializeMinigameInstance(
        minigameDefinition: MinigameDefinition,
        teams: List<MinigameTeam>,
    ): Result<MinigameInstance, MinigameInitError> {
        val playersInAMinigame =
            teams
                .map { team -> team.players.filter { player -> isPlayerInMinigame(player) } }
                .flatten()

        if (!playersInAMinigame.isEmpty()) {
            return Err(MinigameInitError.PlayerAlreadyInMinigame(playersInAMinigame))
        }

        val minigameInstance = minigameDefinition.createInstance(teams)

        inFlightMinigames.add(minigameInstance)

        return Ok(minigameInstance)
    }

    fun handleMinigameSetup(minigameInstance: MinigameInstance) {
        SuperSmashMobsBrawl.instance.launch {
            minigameInstance.initMinigame().onFailure { minigameInstance.teardownMinigame() }
        }
    }

    fun removeMinigameInstance(minigameInstance: MinigameInstance): Boolean {
        return inFlightMinigames.remove(minigameInstance)
    }

    fun isPlayerInMinigame(player: Player): Boolean {
        return inFlightMinigames.find { it.isPlayerInMinigame(player) } != null
    }

    fun getMinigameForPlayer(player: Player): MinigameInstance? {
        return inFlightMinigames.find { it.isPlayerInMinigame(player) }
    }

    fun handlePlayerLeave(player: Player): Result<Unit, MinigameLeaveError> {
        val minigameInstance =
            getMinigameForPlayer(player) ?: return Err(MinigameLeaveError.PlayerNotInMinigame)

        return try {
            minigameInstance
                .onPlayerLeave(player)
                .onSuccess {
                    // Try to move player back to hub
                    val hubResult = HubService.tryTeleportToDefaultHub(player)
                    if (hubResult.isFailure) {
                        return Err(MinigameLeaveError.HubNotReady)
                    }
                }
                .onFailure {
                    return Err(MinigameLeaveError.NotAllowedToLeave)
                }

            return Ok(Unit)
        } catch (e: Exception) {
            Err(MinigameLeaveError.Unknown)
        }
    }
}
