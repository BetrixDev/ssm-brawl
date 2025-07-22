package dev.betrix.superSmashMobsBrawl.services

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import dev.betrix.superSmashMobsBrawl.minigames.instances.MinigameInstance
import dev.betrix.superSmashMobsBrawl.models.MinigameTeam
import org.bukkit.entity.Player
import kotlin.onFailure

sealed class MinigameInitError {
    data class PlayerAlreadyInMinigame(val players: List<Player>): MinigameInitError()
}

object MinigameService {
    private val inFlightMinigames = arrayListOf<MinigameInstance>()

    fun initializeMinigameInstance(minigameDefinition: MinigameDefinition, teams: List<MinigameTeam>): Result<MinigameInstance, MinigameInitError> {
        val playersInAMinigame = teams
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
        minigameInstance.setup().onFailure {
            minigameInstance.teardown()
        }
    }

    fun removeMinigameInstance(minigameInstance: MinigameInstance): Boolean {
        return inFlightMinigames.remove(minigameInstance)
    }

    fun isPlayerInMinigame(player: Player): Boolean {
        return inFlightMinigames.find { it.isPlayerInMinigame(player) } != null
    }
}