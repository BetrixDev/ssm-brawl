package dev.betrix.superSmashMobsBrawl.minigames

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.github.michaelbull.result.unwrap
import com.github.shynixn.mccoroutine.bukkit.launch
import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.events.BrawlDeathEvent
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.events.DeathReason
import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent
import dev.betrix.superSmashMobsBrawl.extensions.doKnockback
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
import gg.flyte.twilight.event.event
import gg.flyte.twilight.extension.feed
import gg.flyte.twilight.extension.heal
import gg.flyte.twilight.scheduler.repeatingTask
import java.time.Duration
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
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

    @Suppress("UNCHECKED_CAST")
    protected val minigameData: TMinigameDef by lazy {
        (dataService.getMinigame(minigameId)
            ?: throw RuntimeException("Could not find minigame data for id $minigameId"))
            as TMinigameDef
    }

    lateinit var brawlWorld: BrawlGameWorld
        protected set

    protected val assignedKits = mutableListOf<Pair<Player, BrawlKit>>()

    var state = MinigameState.PREFLIGHT
        protected set

    /** Determine if a passive can be used in a minigame */
    fun isPassiveValid(id: String): Boolean {
        return minigameData.isPassiveValid(id)
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

        // Apply damage within the context of this minigame
        listeners.add(
            event<SmashDamageEvent> {
                // Only handle if this damage concerns players in this minigame
                @Suppress("UNCHECKED_CAST")
                if (!isValid(this@BrawlMinigame as BrawlMinigame<MinigameDef>)) return@event

                val victimPlayer = victim as? Player ?: return@event

                if (victimPlayer.gameMode != GameMode.SURVIVAL) return@event

                val startingHealth = victimPlayer.health
                val newHealth = (startingHealth - damage).coerceAtLeast(0.0)

                if (newHealth <= 0.0) {
                    // Prevent vanilla death and route through our brawl death flow
                    victimPlayer.health = 1.0
                    BrawlDeathEvent.call(victimPlayer, DeathReason.Damage)
                } else {
                    victimPlayer.health = newHealth
                }

                // Apply 1.8-style melee knockback only for melee damage (no special damage type)
                if (damageType == null) {
                    val damagerPlayer =
                        (damager as? Damager.DamagerLivingEntity)?.livingEntity as? Player
                    if (damagerPlayer != null) {
                        val kitKnockbackMult =
                            kitService.getKitForPlayer(damagerPlayer)?.let {
                                dataService.getKit(it.id)?.knockbackMultiplier
                            } ?: 1.0

                        victimPlayer.noDamageTicks = 0
                        victimPlayer.doKnockback(
                            knockbackMultiplier * kitKnockbackMult,
                            damage,
                            startingHealth,
                            damagerPlayer.location.toVector(),
                            null,
                        )
                    }
                }
            }
        )

        // Translate melee damage into SmashDamageEvent within this minigame
        listeners.add(
            event<EntityDamageByEntityEvent> {
                if (isCancelled) return@event

                val victimPlayer = entity as? Player ?: return@event
                val damagerPlayer = damager as? Player ?: return@event

                if (!hasPlayer(victimPlayer) || !hasPlayer(damagerPlayer)) return@event

                if (
                    cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK &&
                        cause != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK
                )
                    return@event

                if (victimPlayer.gameMode != GameMode.SURVIVAL) return@event

                // Cancel vanilla damage and route through SmashDamageEvent using kit melee damage
                isCancelled = true

                val attackerKit = kitService.getKitForPlayer(damagerPlayer)
                val meleeDamage = attackerKit?.getMeleeDamage() ?: damage

                SmashDamageEvent(
                        victimPlayer,
                        Damager.DamagerLivingEntity(damagerPlayer),
                        meleeDamage,
                    )
                    .callEvent()
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
            player.teleport(brawlWorld.world.location(spawnPoints[min(idx, spawnPoints.size)]))
            assignPlayerKit(player)
            // Clear offhand to remove shield mechanics (1.8 feel)
            try {
                player.inventory.setItemInOffHand(
                    org.bukkit.inventory.ItemStack.of(org.bukkit.Material.AIR)
                )
            } catch (_: Throwable) {}
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

                kotlinx.coroutines.delay(1.seconds)
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
        assignedKits.removeIf { it.first == player }
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
            gg.flyte.twilight.scheduler.repeatingTask(5) {
                players.forEach { player ->
                    if (player.gameMode == GameMode.SURVIVAL && player.location.y <= voidLevel) {
                        plugin.logger.info("Player $player fell into the void")
                        dev.betrix.superSmashMobsBrawl.events.BrawlDeathEvent.call(
                            player,
                            dev.betrix.superSmashMobsBrawl.events.DeathReason.Void,
                        )
                    }
                }
            }
        )

        return Ok(Unit)
    }

    private suspend fun findAndCreateWorld(): Result<BrawlGameWorld, Exception> {
        val validMaps =
            dataService.getAllGameMaps().filter { map ->
                minigameData.mapWhitelist?.let { whitelist ->
                    return@filter whitelist.contains(map.id)
                }
                minigameData.mapBlacklist?.let { blacklist ->
                    return@filter !blacklist.contains(map.id)
                }
                true
            }

        if (validMaps.isEmpty()) {
            return Err(RuntimeException("No valid maps found for $minigameData"))
        }

        val selectedMap = validMaps.random()

        return worldService.copyAndLoadWorld(selectedMap, gameId).map { it as BrawlGameWorld }
    }
}
