package dev.betrix.superSmashMobsBrawl.minigames.instances

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dev.betrix.superSmashMobsBrawl.kits.definitions.CreeperKitDefinition
import dev.betrix.superSmashMobsBrawl.kits.instances.KitInstance
import dev.betrix.superSmashMobsBrawl.maps.campsiteMap
import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import dev.betrix.superSmashMobsBrawl.models.MinigameTeam
import dev.betrix.superSmashMobsBrawl.utils.WorldUtils
import dev.betrix.superSmashMobsBrawl.utils.createLocation
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.UUID

class TestingMinigameInstance(
    definition: MinigameDefinition,
    teams: List<MinigameTeam>
) : MinigameInstance(definition, teams) {
    private val playerKits = hashMapOf<Player, KitInstance>()
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
                        player.teleport(createLocation(world, campsiteMap.spawnPoints[0]))
                    }
                }

            teams.forEach { team ->
                team.players.forEach { player ->
                    playerKits[player] = CreeperKitDefinition.createInstance(player)
                }
            }

            playerKits.values.forEach { it.setup() }

            return Ok(Unit)
        } catch (e: Exception) {
            return Err(e)
        }
    }

    override fun teardown() {
        WorldUtils.deleteWorld(world)
        playerKits.values.forEach { it.teardown() }

        // TODO: finish
    }

    override fun onPlayerLeave(player: Player) {
        TODO("Not yet implemented")
    }
}