package dev.betrix.superSmashMobsBrawl.minigames.instances

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dev.betrix.superSmashMobsBrawl.kits.definitions.CreeperKitDefinition
import dev.betrix.superSmashMobsBrawl.maps.campsiteMap
import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import dev.betrix.superSmashMobsBrawl.models.MinigameTeam
import dev.betrix.superSmashMobsBrawl.services.KitService
import dev.betrix.superSmashMobsBrawl.utils.WorldUtils
import dev.betrix.superSmashMobsBrawl.utils.createLocation
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.UUID
import gg.flyte.twilight.extension.feed
import gg.flyte.twilight.extension.heal
import dev.betrix.superSmashMobsBrawl.services.MinigameService
import dev.betrix.superSmashMobsBrawl.services.HubService

class TestingMinigameInstance(
    definition: MinigameDefinition,
    teams: List<MinigameTeam>
) : MinigameInstance(definition, teams) {
    override lateinit var world: World

    private val players: List<Player>
        get() = teams.flatMap { it.players }

    override fun setup(): Result<Unit, Exception> {
        try {
            val gameId = UUID.randomUUID().toString()

            WorldUtils.copyAndLoadWorld(campsiteMap.id, gameId)
                .onFailure { err -> return Err(err) }
                .onSuccess { world ->
                    this@TestingMinigameInstance.world = world

                    players.forEach { player ->
                        player.feed()
                        player.heal()
                        player.teleport(createLocation(world, campsiteMap.spawnPoints[0]))
                    }
                }

            // Assign kits to all players
            teams.forEach { team ->
                team.players.forEach { player ->
                    KitService.assignKit(player, CreeperKitDefinition)
                        .onFailure { error ->
                            // Log error if kit assignment fails
                            println("Failed to assign kit to ${player.name}: $error")
                        }
                }
            }

            return Ok(Unit)
        } catch (e: Exception) {
            return Err(e)
        }
    }

    override fun teardown() {
        // Unassign kits from all players
        players.forEach { player ->
            HubService.teleportToDefaultHub(player)
            KitService.unassignKit(player)
        }
        
        WorldUtils.deleteWorldAsync(world)
        MinigameService.removeMinigameInstance(this)
    }

    override fun onPlayerLeave(player: Player): Result<Unit, String> {
        // Remove the player from teams
        teams.forEach { team ->
            team.players.remove(player)
        }
        
        // Unassign kit
        KitService.unassignKit(player)

        // Check if minigame should end and clean up
        if (shouldEndMinigame()) {
            teardown()
        }

        return Ok(Unit)
    }

    private fun shouldEndMinigame(): Boolean {
        if (players.size == 0) {
            return true
        }

        return false
    }
}