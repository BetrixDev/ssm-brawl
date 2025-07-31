package dev.betrix.superSmashMobsBrawl.minigames.instances

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.github.shynixn.mccoroutine.bukkit.launch
import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.minigames.MinigameState
import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import dev.betrix.superSmashMobsBrawl.models.MinigameTeam
import dev.betrix.superSmashMobsBrawl.registries.MapRegistry
import dev.betrix.superSmashMobsBrawl.services.HubService
import dev.betrix.superSmashMobsBrawl.services.KitService
import dev.betrix.superSmashMobsBrawl.services.WorldService
import java.util.UUID
import org.bukkit.World
import org.bukkit.entity.Player

abstract class MinigameInstance(val definition: MinigameDefinition, val teams: List<MinigameTeam>): Manageable() {
    open lateinit var world: World
        protected set
    var state = MinigameState.PREFLIGHT
        protected set

    val gameId = UUID.randomUUID().toString()
    val map = MapRegistry.getValidMapsForMinigame(definition).random()

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

            WorldService.copyAndLoadWorld(map, gameId)
                .onFailure { return Err(it) }
                .onSuccess { world = it.world }

        } catch (e: Exception) {
            return Err(e)
        }

        setupAsync()
        return Ok(Unit)
    }

    override suspend fun teardownAsync() {
        WorldService.deleteWorld(world).onFailure {
            SuperSmashMobsBrawl.instance.logger.severe("Unable to delete world $world")
        }

        players.forEach { player ->
            HubService.teleportToDefaultHub(player)
        }

        super.teardownAsync()
    }

    open suspend fun teardownMinigame() {
        teardownAsync()
    }

    /**
     * Called when a player requests to leave the minigame.
     * Minigames should override this method to implement game-specific leave policies.
     * 
     * The default implementation provides sensible state-based behavior:
     * - PREFLIGHT/STARTING: Always allow leaving
     * - ONGOING: Allow leaving but minigames can override for competitive rules
     * - ENDED: Always allow leaving
     * 
     * @param player The player requesting to leave
     * @return Result.Ok if approved, Result.Err with denial reason if denied
     */
    open fun onPlayerLeaveRequest(player: Player): Result<Unit, LeaveRequestDenialReason> {
        return when (state) {
            MinigameState.PREFLIGHT, MinigameState.STARTING -> {
                // Always allow leaving during setup phase
                Ok(Unit)
            }
            MinigameState.ONGOING -> {
                // Default to allowing leave during gameplay
                // Specific minigames can override this for competitive restrictions
                Ok(Unit)
            }
            MinigameState.ENDED -> {
                // Always allow leaving after game ends
                Ok(Unit)
            }
        }
    }

    /**
     * Called when a player's leave request has been approved and the player is actually leaving.
     * This handles the cleanup logic for removing the player from the minigame.
     * 
     * Minigames can override this to add custom cleanup logic, but should call super.onPlayerLeave(player).
     * 
     * @param player The player that is leaving the minigame
     */
    open fun onPlayerLeave(player: Player) {
        // Remove the player from teams
        teams.forEach { team -> team.players.remove(player) }

        // Unassign kit
        KitService.unassignKit(player)

        // Check if minigame should end and clean up
        if (shouldEndMinigame()) {
            SuperSmashMobsBrawl.instance.launch {
                onMinigameEnd()
            }
        }
    }

    open suspend fun onMinigameEnd() {}

    protected abstract fun shouldEndMinigame(): Boolean

    fun isPlayerInMinigame(player: Player): Boolean {
        return teams.find { it.players.contains(player) } != null
    }
}
