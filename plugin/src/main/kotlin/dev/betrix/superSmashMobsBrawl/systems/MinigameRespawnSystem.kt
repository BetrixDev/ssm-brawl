package dev.betrix.superSmashMobsBrawl.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import dev.betrix.superSmashMobsBrawl.components.*
import dev.betrix.superSmashMobsBrawl.events.BrawlDeathEvent
import dev.betrix.superSmashMobsBrawl.extensions.ecsEntity
import dev.betrix.superSmashMobsBrawl.extensions.getFarthestFromPlayers
import dev.betrix.superSmashMobsBrawl.extensions.location
import dev.betrix.superSmashMobsBrawl.models.BrawlGameWorld
import dev.betrix.superSmashMobsBrawl.models.SpawnPoint
import dev.betrix.superSmashMobsBrawl.models.brawlData.KitSwitchingMode
import dev.betrix.superSmashMobsBrawl.models.brawlData.TeamBasedStocksMinigameDef
import dev.betrix.superSmashMobsBrawl.services.KitService
import dev.betrix.superSmashMobsBrawl.services.LangService
import gg.flyte.twilight.event.event
import gg.flyte.twilight.extension.feed
import gg.flyte.twilight.extension.heal
import java.time.Duration
import kotlin.math.max
import net.kyori.adventure.title.Title
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.entity.Player

/** Handles BrawlDeathEvent and respawn countdown/logic for ECS minigames. */
class MinigameRespawnSystem(
    private val lang: LangService = inject(),
    private val kitService: KitService = inject(),
) : IntervalSystem(interval = com.github.quillraven.fleks.Fixed(0.05f)) {

    private val respawning = family {
        all(PlayerComponent, InMinigameComponent, RespawningComponent)
    }

    init {
        // Listen for deaths that occur within any ECS minigame
        event<BrawlDeathEvent> {
            val player = player
            val entity = player.ecsEntity ?: return@event
            val inMinigame = with(world) { entity.getOrNull(InMinigameComponent) } ?: return@event
            val minigame = with(world) { inMinigame.minigameEntity[MinigameComponent] }
            if (!minigame.hasLoadedWorld()) return@event
            if (minigame.state == MinigameState.ENDING) return@event

            // Unassign kit immediately on death
            kitService.unassignKit(player)

            // Effects
            player.world.strikeLightningEffect(player.location)
            gg.flyte.twilight.scheduler.delay(1) {
                player.playSound(player.eyeLocation, Sound.ENTITY_PLAYER_HURT, 1f, 1f)
            }

            // Team-based stocks: consume team life. If team out of stocks, eliminate the dying
            // player.
            val def = minigame.minigame
            if (def is TeamBasedStocksMinigameDef) {
                val teamComp = with(world) { entity.getOrNull(InTeamComponent) }
                val teamsComp =
                    with(world) { inMinigame.minigameEntity.getOrNull(TeamMinigameComponent) }
                val team = teamComp?.let { teamsComp?.getTeam(it.teamId) }
                if (team != null) {
                    if (team.stocks > 0) team.stocks -= 1
                    if (team.stocks == 0) {
                        eliminatePlayer(player, minigame)
                        return@event
                    }
                }
            }

            // Start respawn flow or instantly respawn
            val delaySeconds = minigame.minigame.respawnDelaySeconds
            if (delaySeconds != null && delaySeconds > 0) {
                startRespawnCountdown(entity, minigame, delaySeconds)
            } else {
                respawnNow(entity, minigame)
            }
        }
    }

    override fun onTick() {
        // Tick down respawn timers and show countdown titles
        respawning.forEach { entity ->
            val player = entity[PlayerComponent].player
            val resp = entity[RespawningComponent]
            val before = resp.remainingTicks
            resp.remainingTicks = max(0, resp.remainingTicks - 1)

            if (before / 20 != resp.remainingTicks / 20) {
                val secondsLeft = resp.remainingTicks / 20
                showRespawnTitle(player, secondsLeft)
            }

            if (resp.remainingTicks == 0) {
                // Finalize respawn
                entity.configure { it -= RespawningComponent }
                val minigame = entity[InMinigameComponent].minigameEntity[MinigameComponent]
                respawnNow(entity, minigame)
            }
        }
    }

    private fun showRespawnTitle(player: Player, secondsLeft: Int) {
        val title =
            Title.title(
                lang.t("messages.minigames.respawn.timeLeft") { "secondsLeft" to secondsLeft },
                net.kyori.adventure.text.Component.empty(),
                Title.Times.times(
                    Duration.ofMillis(250),
                    Duration.ofMillis(500),
                    Duration.ofMillis(250),
                ),
            )
        player.showTitle(title)
    }

    private fun startRespawnCountdown(
        entity: Entity,
        minigame: MinigameComponent,
        delaySeconds: Int,
    ) {
        val player = entity[PlayerComponent].player
        val world = minigame.loadedWorld as? BrawlGameWorld ?: return

        // Teleport to spectator point in the game world
        player.teleport(world.world.location(world.data.spectatorSpawnPoint))
        player.gameMode = GameMode.SPECTATOR
        player.allowFlight = true
        player.isFlying = true
        player.fallDistance = 0f

        entity.configure { it += RespawningComponent(delaySeconds * 20) }

        // Initial title
        showRespawnTitle(player, delaySeconds)
    }

    private fun respawnNow(entity: Entity, minigame: MinigameComponent) {
        val player = entity[PlayerComponent].player
        val world = minigame.loadedWorld as? BrawlGameWorld ?: return

        // Choose spawn farthest from active players
        val others = mutableListOf<Player>()
        minigame.playerEntities.forEach { pEntity ->
            if (pEntity == entity) return@forEach
            val p = pEntity[PlayerComponent].player
            if (p.isOnline && p.gameMode == GameMode.SURVIVAL) others.add(p)
        }

        val spawnPoint =
            world.data.spawnPoints.getFarthestFromPlayers(others, world.world)
                ?: SpawnPoint(0.0, 100.0, 0.0)

        player.teleport(world.world.location(spawnPoint))
        player.feed()
        player.heal()
        player.gameMode = GameMode.SURVIVAL

        // Handle kit switching modes on respawn
        val mode = minigame.minigame.kitSwitchingMode
        if (mode == KitSwitchingMode.ON_DEATH || mode == KitSwitchingMode.IMMEDIATE) {
            val currentKit = kitService.getKitForPlayer(player)
            val selectedKit = kitService.currentSelectedKitForPlayer(player)
            if (currentKit?.id != selectedKit.id) {
                kitService.unassignKit(player)
            }
        }

        // Re-assign kit (ensure no duplicate assignment)
        kitService.unassignKit(player)
        kitService.assignKit(player)
    }

    private fun eliminatePlayer(player: Player, minigame: MinigameComponent) {
        val world = minigame.loadedWorld as? BrawlGameWorld ?: return
        player.teleport(world.world.location(world.data.spectatorSpawnPoint))
        player.gameMode = GameMode.SPECTATOR
        player.allowFlight = true
        player.isFlying = true
        player.fallDistance = 0f
        player.feed()
        player.heal()
    }
}
