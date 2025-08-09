package dev.betrix.superSmashMobsBrawl.minigames

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.github.michaelbull.result.unwrap
import com.github.shynixn.mccoroutine.bukkit.launch
import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.events.BrawlDeathEvent
import dev.betrix.superSmashMobsBrawl.events.DeathReason
import dev.betrix.superSmashMobsBrawl.extensions.getEquidistant
import dev.betrix.superSmashMobsBrawl.extensions.getFarthestFromPlayers
import dev.betrix.superSmashMobsBrawl.extensions.location
import dev.betrix.superSmashMobsBrawl.extensions.teleport
import dev.betrix.superSmashMobsBrawl.kits.BrawlKit
import dev.betrix.superSmashMobsBrawl.models.BrawlGameWorld
import dev.betrix.superSmashMobsBrawl.models.MinigameState
import dev.betrix.superSmashMobsBrawl.models.SpawnPoint
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import dev.betrix.superSmashMobsBrawl.services.*
import gg.flyte.twilight.extension.feed
import gg.flyte.twilight.extension.heal
import gg.flyte.twilight.scheduler.repeatingTask
import java.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class BrawlMinigame<TMinigameDef : MinigameDef>(
    public val minigameId: String,
    protected val gameId: String,
    protected val players: List<Player>,
) : Manageable(), KoinComponent {
    protected val dataService: DataService by inject()
    private val kitService: KitService by inject()
    private val worldService: WorldService by inject()
    private val langService: LangService by inject()
    private val plugin: SuperSmashMobsBrawl by inject()

    protected val minigameData =
        (dataService.getMinigame(minigameId) as TMinigameDef?)
            ?: throw RuntimeException("Could not find minigame data for id $minigameId")

    lateinit var brawlWorld: BrawlGameWorld
        protected set

    protected val assignedKits = mutableListOf<Pair<Player, BrawlKit>>()

    var state = MinigameState.PREFLIGHT
        protected set

    /** Determine if a passive can be used in a minigame */
    fun isPassiveValid(id: String): Boolean {
        return minigameData.isPasiveValid(id)
    }

    fun hasPlayer(player: Player): Boolean {
        return players.contains(player)
    }

    open suspend fun initMinigame(): Result<Unit, Exception> {
        listeners.add(
            BrawlDeathEvent.listen(this) {
                plugin.logger.info("Player $player died")
                plugin.launch { onPlayerDeath(player) }
            }
        )

        brawlWorld =
            findAndCreateWorld()
                .onFailure {
                    return Err(it)
                }
                .unwrap()

        createVoidDeathListener().onFailure {
            return Err(it)
        }

        val spawnPoints = brawlWorld.data.spawnPoints.getEquidistant(players.size)

        players.forEachIndexed { idx, player ->
            player.teleport(brawlWorld.world.location(spawnPoints[idx]))
            assignPlayerKit(player)
        }

        state = MinigameState.STARTING

        return Ok(Unit)
    }

    open suspend fun onPlayerDeath(player: Player) {
        kitService.unassignKit(player)?.let { assignedKits.removeIf { it.first == player } }

        if (minigameData.respawnDelaySeconds != null) {
            player.teleport(brawlWorld.data.spectatorSpawnPoint)
            player.gameMode = GameMode.SPECTATOR
            player.allowFlight = true
            player.isFlying = true

            val respawnDelay = minigameData.respawnDelaySeconds ?: 0

            repeat(respawnDelay) { iteration ->
                val secondsLeft = respawnDelay - iteration

                val title =
                    Title.title(
                        langService.t("messages.minigames.respawn.timeLeft") {
                            "secondsLeft" to secondsLeft
                        },
                        Component.empty(),
                        Title.Times.times(
                            Duration.ofMillis(250),
                            Duration.ofMillis(500),
                            Duration.ofMillis(250),
                        ),
                    )

                player.showTitle(title)

                withContext(Dispatchers.IO) { delay(1.seconds) }
            }
        }

        val spawnPoint =
            brawlWorld.data.spawnPoints.getFarthestFromPlayers(
                players.filter { it != player },
                brawlWorld.world,
            ) ?: SpawnPoint(100.0, 100.0, 100.0)

        player.teleport(spawnPoint)
        player.feed()
        player.heal()
        player.gameMode = GameMode.SURVIVAL
        assignPlayerKit(player)
    }

    fun isPlayerInMinigame(player: Player): Boolean {
        return players.contains(player)
    }

    open fun canPlayerLeaveMinigame(player: Player): Boolean {
        return true
    }

    open fun onPlayerLeave(player: Player) {
        kitService.unassignKit(player)
    }

    private fun assignPlayerKit(player: Player) {
        kitService
            .assignKit(player)
            .onFailure { err ->
                when (err) {
                    AssignKitError.PLAYER_HAS_KIT -> {
                        kitService.unassignKit(player)
                        kitService.assignKit(player)
                    }
                }
            }
            .onSuccess { kit -> assignedKits.add(Pair(player, kit)) }
    }

    private fun createVoidDeathListener(): Result<Unit, Exception> {
        if (!::brawlWorld.isInitialized) {
            return Err(
                RuntimeException("This function was called before brawlWorld finished loading")
            )
        }

        val voidLevel = brawlWorld.data.voidLevel

        runnables.add(
            repeatingTask(5) {
                players.forEach { player ->
                    if (player.gameMode == GameMode.SURVIVAL && player.location.y <= voidLevel) {
                        plugin.logger.info("Player $player fell into the void")
                        BrawlDeathEvent.call(player, DeathReason.Void)
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

        return worldService.copyAndLoadWorld(selectedMap, gameId)
    }
}
