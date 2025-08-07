package dev.betrix.superSmashMobsBrawl.minigames

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.github.michaelbull.result.unwrap
import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.extensions.getEquidistant
import dev.betrix.superSmashMobsBrawl.extensions.location
import dev.betrix.superSmashMobsBrawl.kits.instances.KitInstance
import dev.betrix.superSmashMobsBrawl.models.BrawlGameWorld
import dev.betrix.superSmashMobsBrawl.models.MinigameState
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import dev.betrix.superSmashMobsBrawl.services.AssignKitError
import dev.betrix.superSmashMobsBrawl.services.DataService
import dev.betrix.superSmashMobsBrawl.services.KitService
import dev.betrix.superSmashMobsBrawl.services.WorldService
import gg.flyte.twilight.extension.kill
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class BrawlMinigame<TMinigameDef : MinigameDef>(
    minigameId: String,
    protected val gameId: String,
    protected val players: List<Player>,
) : Manageable(), KoinComponent {
    protected val dataService: DataService by inject()
    private val kitService: KitService by inject()
    private val worldService: WorldService by inject()

    protected val minigameData =
        (dataService.getMinigame(minigameId) as TMinigameDef?)
            ?: throw RuntimeException("Could not find minigame data for id $minigameId")

    lateinit var brawlWorld: BrawlGameWorld
        protected set
    protected val assignedKits = mutableListOf<Pair<Player, KitInstance>>()

    var state = MinigameState.PREFLIGHT
        protected set

    open suspend fun initMinigame(): Result<Unit, Exception> {
        brawlWorld = findAndCreateWorld().onFailure { return Err(it) }.unwrap()

        createVoidDeathListener().onFailure { return Err(it) }

        val spawnPoints = brawlWorld.data.spawnPoints.getEquidistant(players.size)

        players.forEachIndexed { idx, player ->
            player.teleport(brawlWorld.world.location(spawnPoints[idx]))

            kitService
                .assignKit(player)
                .onFailure { err ->
                    return when (err) {
                        AssignKitError.PLAYER_HAS_KIT ->
                            Err(
                                RuntimeException(
                                    "Player $player already has a kit assigned to them"
                                )
                            )
                    }
                }
                .onSuccess { kit -> assignedKits.add(Pair(player, kit)) }
        }

        state = MinigameState.STARTING

        return Ok(Unit)
    }

    abstract fun canPlayerLeaveMinigame(player: Player): Boolean

    abstract fun onPlayerLeave(player: Player)

    private fun createVoidDeathListener(): Result<Unit, Exception> {
        if (!::brawlWorld.isInitialized) {
            return Err(RuntimeException("This function was called before brawlWorld finished loading"))
        }

        val voidLevel = brawlWorld.data.voidLevel

        runnables.add(
            repeatingTask(5) {
                players.forEach { player ->
                    if (player.location.y <= voidLevel) {
                        player.kill()
                    }
                }
            }
        )

        return Ok(Unit)
    }

    private suspend fun findAndCreateWorld(): Result<BrawlGameWorld, Exception> {
        val validMaps =
            dataService.getAllGameMaps().filter { map ->
                if (minigameData.mapWhitelist?.contains(map.id) == true) {
                    return@filter true
                }

                if (minigameData.mapBlacklist?.contains(map.id) == true) {
                    return@filter false
                }

                return@filter true
            }

        if (validMaps.isEmpty()) {
            return Err(RuntimeException("No valid maps found for $minigameData"))
        }

        val selectedMap = validMaps.random()

        return worldService.copyAndLoadWorld<BrawlGameWorld>(selectedMap, gameId)
    }
}
