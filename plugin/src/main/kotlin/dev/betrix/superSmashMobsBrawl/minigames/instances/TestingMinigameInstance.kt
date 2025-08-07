package dev.betrix.superSmashMobsBrawl.minigames.instances

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import dev.betrix.superSmashMobsBrawl.models.MinigameTeam
import dev.betrix.superSmashMobsBrawl.services.KitService
import dev.betrix.superSmashMobsBrawl.utils.createLocation
import gg.flyte.twilight.extension.feed
import gg.flyte.twilight.extension.heal
import org.bukkit.World

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

        return Ok(Unit)
    }

    override suspend fun teardownMinigame() {
        val playersToCleanup = teams.flatMap { it.players.toList() }

        //        // Unassign kits from all players
        //        withContext(SuperSmashMobsBrawl.instance.minecraftDispatcher) {
        //            playersToCleanup.forEach { player ->
        //                HubService.teleportToDefaultHub(player)
        //                KitService.unassignKit(player)
        //            }
        //
        //            WorldService.deleteWorld(world)
        //            MinigameService.removeMinigameInstance(this@TestingMinigameInstance)
        //        }
    }

    override fun shouldEndMinigame(): Boolean {
        // Create safe copy to avoid ConcurrentModificationException
        return teams.flatMap { it.players }.isEmpty()
    }
}
