package dev.betrix.superSmashMobsBrawl.minigames.instances

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.mapBoth
import com.github.michaelbull.result.onFailure
import com.github.shynixn.mccoroutine.bukkit.launch
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.kits.definitions.CreeperKitDefinition
import dev.betrix.superSmashMobsBrawl.maps.campsiteMap
import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import dev.betrix.superSmashMobsBrawl.models.MinigameTeam
import dev.betrix.superSmashMobsBrawl.services.HubService
import dev.betrix.superSmashMobsBrawl.services.KitService
import dev.betrix.superSmashMobsBrawl.services.MinigameService
import dev.betrix.superSmashMobsBrawl.services.WorldService
import dev.betrix.superSmashMobsBrawl.utils.createLocation
import gg.flyte.twilight.extension.feed
import gg.flyte.twilight.extension.heal
import org.bukkit.World
import org.bukkit.entity.Player

class TestingMinigameInstance(definition: MinigameDefinition, teams: List<MinigameTeam>) :
    MinigameInstance(definition, teams) {
    override lateinit var world: World

    private val players: List<Player>
        get() = teams.flatMap { it.players }

    override suspend fun setupMinigame(): Result<Unit, Exception> {
        try {
            WorldService.copyAndLoadWorld(campsiteMap, gameId)
                .mapBoth(
                    failure = {
                        return Err(it)
                    },
                    success = { loadedWorld ->
                        this@TestingMinigameInstance.world = loadedWorld.world

                        players.forEach { player ->
                            player.feed()
                            player.heal()
                            player.teleport(createLocation(world, campsiteMap.spawnPoints[0]))
                        }
                    },
                )

            // Assign kits to all players
            teams.forEach { team ->
                team.players.forEach { player ->
                    KitService.assignKit(player, CreeperKitDefinition).onFailure { error ->
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

    override suspend fun teardown() {
        // Unassign kits from all players
        players.forEach { player ->
            HubService.teleportToDefaultHub(player)
            KitService.unassignKit(player)
        }

        WorldService.deleteWorld(world)
        MinigameService.removeMinigameInstance(this)
    }

    override fun onPlayerLeave(player: Player): Result<Unit, String> {
        // Remove the player from teams
        teams.forEach { team -> team.players.remove(player) }

        // Unassign kit
        KitService.unassignKit(player)

        // Check if minigame should end and clean up
        if (shouldEndMinigame()) {
            SuperSmashMobsBrawl.instance.launch { teardown() }
        }

        return Ok(Unit)
    }

    private fun shouldEndMinigame(): Boolean {
        return players.isEmpty()
    }
}
