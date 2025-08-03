package dev.betrix.superSmashMobsBrawl.minigames.instances

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import dev.betrix.superSmashMobsBrawl.models.MinigameTeam
import dev.betrix.superSmashMobsBrawl.services.HubService
import dev.betrix.superSmashMobsBrawl.services.KitService
import dev.betrix.superSmashMobsBrawl.services.MinigameService
import dev.betrix.superSmashMobsBrawl.services.WorldService
import dev.betrix.superSmashMobsBrawl.utils.createLocation
import gg.flyte.twilight.event.event
import gg.flyte.twilight.extension.feed
import gg.flyte.twilight.extension.heal
import kotlinx.coroutines.withContext
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent

class TestingMinigameInstance(definition: MinigameDefinition, teams: List<MinigameTeam>) :
    MinigameInstance(definition, teams) {
    override lateinit var world: World

    override suspend fun initMinigame(): Result<Unit, Exception> {
        super.initMinigame().onFailure {
            return Err(it)
        }

        teams.forEach { team ->
            team.players.forEach { player ->
                player.feed()
                player.heal()
                player.teleport(createLocation(world, map.spawnPoints[0]))

                KitService.assignKit(player, definition).onFailure { error ->
                    // Log error if kit assignment fails
                    SuperSmashMobsBrawl.instance.logger.warning(
                        "Failed to assign kit to ${player.name}: $error"
                    )
                }
            }
        }

        setupEventListeners()

        return Ok(Unit)
    }

    override suspend fun teardownMinigame() {
        val playersToCleanup = teams.flatMap { it.players.toList() }
        
        // Unassign kits from all players
        withContext(SuperSmashMobsBrawl.instance.minecraftDispatcher) {
            playersToCleanup.forEach { player ->
                HubService.teleportToDefaultHub(player)
                KitService.unassignKit(player)
            }

            WorldService.deleteWorld(world)
            MinigameService.removeMinigameInstance(this@TestingMinigameInstance)
        }
    }

    override fun shouldEndMinigame(): Boolean {
        // Create safe copy to avoid ConcurrentModificationException
        return teams.flatMap { it.players }.isEmpty()
    }

    private fun setupEventListeners() {
        // Listen for PlayerDeathEvent to handle instant respawn
        listeners.add(
            event<PlayerDeathEvent> {
                val player = player
                if (!isPlayerInMinigame(player)) return@event

                // Cancel the default death behavior
                isCancelled = true

                handlePlayerDeath(player)
            }
        )
    }

    private fun handlePlayerDeath(player: Player) {
        // Check if player is still in the minigame
        if (!isPlayerInMinigame(player)) {
            return
        }

        // Unassign kit on death
        KitService.unassignKit(player)

        // Respawn player immediately at a random spawn point
        SuperSmashMobsBrawl.instance.launch {
            // Double-check that player is still in the minigame before respawning
            if (!isPlayerInMinigame(player)) {
                return@launch
            }

            val spawnPoints = map.spawnPoints
            if (spawnPoints.isEmpty()) {
                SuperSmashMobsBrawl.instance.logger.warning(
                    "No spawn points available for ${player.name} in testing minigame"
                )
                return@launch
            }

            val randomSpawnPoint = spawnPoints.random()
            val respawnLocation = createLocation(world, randomSpawnPoint)

            // Teleport player to spawn point
            player.teleport(respawnLocation)

            // Heal and feed the player
            player.heal()
            player.feed()

            // Reassign kit
            KitService.assignKit(player, definition).onFailure { error ->
                SuperSmashMobsBrawl.instance.logger.warning(
                    "Failed to reassign kit to ${player.name} after respawn: ${error.name}"
                )
            }
        }
    }
}
