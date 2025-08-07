package dev.betrix.superSmashMobsBrawl.minigames.instances

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.github.shynixn.mccoroutine.bukkit.launch
import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import dev.betrix.superSmashMobsBrawl.models.MinigameState
import dev.betrix.superSmashMobsBrawl.models.MinigameTeam
import dev.betrix.superSmashMobsBrawl.maps.SsmbMap
import dev.betrix.superSmashMobsBrawl.registries.MapRegistry
import dev.betrix.superSmashMobsBrawl.services.HubService
import dev.betrix.superSmashMobsBrawl.services.WorldService
import dev.betrix.superSmashMobsBrawl.services.LoadedWorld
import java.util.UUID
import org.bukkit.World
import org.bukkit.entity.Player

abstract class MinigameInstance(val definition: MinigameDefinition, val teams: List<MinigameTeam>) :
    Manageable() {
    private lateinit var loadedWorld: LoadedWorld

    val world: World
        get() = loadedWorld.world

    var state = MinigameState.PREFLIGHT
        protected set

    val gameId = UUID.randomUUID().toString()
    val map: SsmbMap = MapRegistry.getValidMapsForMinigame(definition).random()

    protected val players: List<Player>
        get() = teams.flatMap { it.players }

    open suspend fun initMinigame(): Result<Unit, Exception> {
        try {
            teams.forEach {
                it.players.forEach { player ->
                    if (!player.isOnline) {
                        return Err(RuntimeException("A player is offline"))
                    }
                }
            }

            WorldService.copyAndLoadWorld(map)
                .onFailure {
                    return Err(it)
                }
                .onSuccess { loadedWorld = it }
        } catch (e: Exception) {
            return Err(e)
        }

        setup()
        return Ok(Unit)
    }

    open suspend fun teardownMinigame() {
        WorldService.deleteWorld(world).onFailure {
            SuperSmashMobsBrawl.instance.logger.severe("Unable to delete world $world")
        }

        players.forEach { player -> HubService.teleportToDefaultHub(player) }

        teardown()
    }

    open fun onPlayerLeave(player: Player): Result<Unit, String> {
        // Remove the player from teams
        teams.forEach { team -> team.players.remove(player) }

        // Check if minigame should end and clean up
        if (shouldEndMinigame()) {
            SuperSmashMobsBrawl.instance.launch { onMinigameEnd() }
        }

        return Ok(Unit)
    }

    open suspend fun onMinigameEnd() {}

    protected abstract fun shouldEndMinigame(): Boolean

    fun isPlayerInMinigame(player: Player): Boolean {
        return teams.find { it.players.contains(player) } != null
    }
}
