package dev.betrix.superSmashMobsBrawl.services

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.shynixn.mccoroutine.bukkit.launch
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.minigames.BrawlMinigame
import dev.betrix.superSmashMobsBrawl.models.MinigameTeam
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import dev.betrix.superSmashMobsBrawl.utils.resultRunCatching
import org.bukkit.entity.Player
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

    private val inFlightMinigames = arrayListOf<BrawlMinigame<*>>()

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

    fun initializeMinigameInstance(
        minigameDefinition: MinigameDef,
        teams: List<MinigameTeam>,
    ): Result<BrawlMinigame<*>, MinigameInitError> {
        return Err(MinigameInitError.PlayerAlreadyInMinigame(emptyList()))
    }

    fun handleMinigameSetup(minigameInstance: BrawlMinigame<*>) {
        plugin.launch { minigameInstance.initMinigame().onFailure { minigameInstance.teardown() } }
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

        hubService.teleportToDefaultHub(player)

        return Ok(minigameInstance)
    }
}
