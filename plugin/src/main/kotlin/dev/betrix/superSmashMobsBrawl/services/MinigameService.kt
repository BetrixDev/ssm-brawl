package dev.betrix.superSmashMobsBrawl.services

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.github.shynixn.mccoroutine.bukkit.launch
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.models.MinigameTeam
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import dev.betrix.superSmashMobsBrawl.utils.mm
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
    private val dataService: DataService by inject()

    // Minimal stub to keep build green until new flow is wired
    interface MinigameInstance {
        suspend fun initMinigame(): Result<Unit, Exception>

        fun teardownMinigame()

        fun onPlayerLeave(player: Player): Result<Unit, Exception>

        fun isPlayerInMinigame(player: Player): Boolean
    }

    private val inFlightMinigames = arrayListOf<MinigameInstance>()

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
    ): Result<MinigameInstance, MinigameInitError> {
        return Err(MinigameInitError.PlayerAlreadyInMinigame(emptyList()))
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
                    KitService.unassignKit(player)

                    HubService.tryTeleportToDefaultHub(player).onFailure {
                        player.kick(mm("<red>We couldn't put you back in the hub</red>"))
                        SuperSmashMobsBrawl.instance.logger.severe(
                            "Failed to teleport ${player.name} to hub: ${it.message}"
                        )
                    }
                }
                .onFailure {
                    return Err(MinigameLeaveError.NotAllowedToLeave)
                }

            return Ok(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Err(MinigameLeaveError.Unknown)
        }
    }
}
