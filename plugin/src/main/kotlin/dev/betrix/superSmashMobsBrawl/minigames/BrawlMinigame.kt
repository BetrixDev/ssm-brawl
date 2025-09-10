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
import dev.betrix.superSmashMobsBrawl.events.PlayerSelectKitEvent
import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent
import dev.betrix.superSmashMobsBrawl.extensions.doKnockback
import dev.betrix.superSmashMobsBrawl.extensions.getEquidistant
import dev.betrix.superSmashMobsBrawl.extensions.getFarthestFromPlayers
import dev.betrix.superSmashMobsBrawl.extensions.location
import dev.betrix.superSmashMobsBrawl.extensions.teleport
import dev.betrix.superSmashMobsBrawl.kits.BrawlKit
import dev.betrix.superSmashMobsBrawl.models.BrawlGameWorld
import dev.betrix.superSmashMobsBrawl.models.MinigamePlayer
import dev.betrix.superSmashMobsBrawl.models.MinigameState
import dev.betrix.superSmashMobsBrawl.models.SpawnPoint
import dev.betrix.superSmashMobsBrawl.models.brawlData.KitSwitchingMode
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import dev.betrix.superSmashMobsBrawl.services.*
import gg.flyte.twilight.event.event
import gg.flyte.twilight.extension.feed
import gg.flyte.twilight.extension.heal
import gg.flyte.twilight.scheduler.repeatingTask
import java.time.Duration
import java.util.UUID
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.GameMode
import org.bukkit.EntityEffect
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class BrawlMinigame<TMinigameDef : MinigameDef>(
    val minigameId: String,
    protected val gameId: String,
    protected val players: List<MinigamePlayer>,
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

    var brawlWorld: BrawlGameWorld? = null
        protected set

    protected val assignedKits = mutableListOf<Pair<Player, BrawlKit>>()

    /** 1.8-style invulnerability frames for melee (10 ticks = 500ms) */
    private val meleeInvulnerabilityMs: Long = 500
    private val lastMeleeHitAtByVictim = mutableMapOf<UUID, Long>()

    var state = MinigameState.PREFLIGHT
        protected set

    /** Tracks players who disconnected while in this minigame */
    protected val disconnectedPlayers = mutableSetOf<UUID>()

    /** Tracks players who are currently dead and waiting to respawn */
    protected val respawningPlayers = mutableSetOf<UUID>()

    /** Determine if a passive can be used in a minigame */
    fun isPassiveValid(id: String): Boolean {
        return minigameData.isPassiveValid(id)
    }

    /** Get the kit switching mode for this minigame */
    fun getKitSwitchingMode(): KitSwitchingMode {
        return minigameData.kitSwitchingMode
    }

    /** Check if a player is currently dead and waiting to respawn */
    fun isPlayerRespawning(player: Player): Boolean {
        return respawningPlayers.contains(player.uniqueId)
    }

    open suspend fun initMinigame(): Result<Unit, Exception> {
        listeners.add(
            BrawlDeathEvent.listen(this) {
                plugin.logger.info("Player $player died")
                plugin.launch { onPlayerDeath(player) }
            }
        )

        // Handle kit selection events
        listeners.add(
            event<PlayerSelectKitEvent> {
                // Only handle if this player is in this minigame
                if (!isPlayerInMinigame(player)) return@event

                when (minigameData.kitSwitchingMode) {
                    KitSwitchingMode.IMMEDIATE -> {
                        if (shouldSwitchImmediately && !isPlayerRespawning(player)) {
                            // Switch kit immediately (but not if player is currently respawning)
                            kitService.unassignKit(player)
                            kitService.assignKit(player, kit.id)
                        }
                    }

                    else -> {}
                }
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

                // Trigger 1.8-style hurtcam/animation and sound on victim
                try {
                    victimPlayer.playEffect(EntityEffect.HURT)
                } catch (_: Throwable) {}
                try {
                    victimPlayer.playSound(victimPlayer.eyeLocation, Sound.ENTITY_PLAYER_HURT, 1f, 1f)
                } catch (_: Throwable) {}

                // Apply 1.8-style melee knockback only for melee damage (no special damage type)
                if (damageType == null) {
                    val damagerPlayer =
                        (damager as? Damager.DamagerLivingEntity)?.livingEntity as? Player
                    if (damagerPlayer != null) {
                        val kitKnockbackMult =
                            kitService.getKitForPlayer(damagerPlayer)?.let {
                                dataService.getKit(it.id)?.knockbackMultiplier
                            } ?: 1.0
                        victimPlayer.doKnockback(
                            knockbackMultiplier * kitKnockbackMult,
                            damage,
                            startingHealth,
                            damagerPlayer.location.toVector(),
                            null,
                        )
                        // Start 1.8-style melee invulnerability window
                        victimPlayer.noDamageTicks = 10
                        lastMeleeHitAtByVictim[victimPlayer.uniqueId] = System.currentTimeMillis()
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

                if (!isPlayerInMinigame(victimPlayer) || !isPlayerInMinigame(damagerPlayer))
                    return@event

                if (
                    cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK &&
                        cause != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK
                )
                    return@event

                if (victimPlayer.gameMode != GameMode.SURVIVAL) return@event

                // Cancel vanilla damage and route through SmashDamageEvent using kit melee damage
                isCancelled = true

                // Enforce 1.8-style hit cooldown on victim (10 ticks)
                val now = System.currentTimeMillis()
                val lastHitAt = lastMeleeHitAtByVictim[victimPlayer.uniqueId]
                if (lastHitAt != null && now - lastHitAt < meleeInvulnerabilityMs) {
                    return@event
                }

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

        val spawnPoints = brawlWorld!!.data.spawnPoints.getEquidistant(players.size)

        players.forEachIndexed { idx, player ->
            player.player.teleport(
                brawlWorld!!.world.location(spawnPoints[min(idx, spawnPoints.lastIndex)])
            )
            assignPlayerKit(player.player)
            // Clear offhand to remove shield mechanics (1.8 feel)
            try {
                player.player.inventory.setItemInOffHand(
                    org.bukkit.inventory.ItemStack.of(org.bukkit.Material.AIR)
                )
            } catch (_: Throwable) {}
        }

        state = MinigameState.STARTING

        return Ok(Unit)
    }

    open suspend fun onPlayerDeath(player: Player) {
        // Check if player is still in the minigame at the start
        if (!isPlayerInMinigame(player)) {
            return
        }

        kitService.unassignKit(player)?.let { assignedKits.removeIf { it.first == player } }

        player.world.strikeLightningEffect(player.location)

        gg.flyte.twilight.scheduler.delay(1) {
            player.playSound(player.eyeLocation, Sound.ENTITY_PLAYER_HURT, 1f, 1f)
        }

        var respawnSuccessful = false

        if (minigameData.respawnDelaySeconds != null) {
            player.teleport(brawlWorld!!.data.spectatorSpawnPoint)
            player.gameMode = GameMode.SPECTATOR
            player.allowFlight = true
            player.isFlying = true
            player.fallDistance = 0f

            // Mark player as respawning
            respawningPlayers.add(player.uniqueId)

            try {
                val respawnDelay = minigameData.respawnDelaySeconds ?: 0

                repeat(respawnDelay) { iteration ->
                    // Check if player is still in the minigame before each countdown step
                    if (!isPlayerInMinigame(player)) {
                        return
                    }

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

                    delay(1.seconds)
                }

                // Final check before respawning
                if (!isPlayerInMinigame(player)) {
                    return
                }

                // If we reach here, respawn was successful
                respawnSuccessful = true
            } finally {
                // Only remove from respawning if respawn failed (player left during countdown)
                if (!respawnSuccessful) {
                    respawningPlayers.remove(player.uniqueId)
                }
            }
        }

        // Remove player from respawning state before respawning (successful case)
        if (respawnSuccessful || minigameData.respawnDelaySeconds == null) {
            respawningPlayers.remove(player.uniqueId)
        }

        val spawnPoint =
            brawlWorld!!
                .data
                .spawnPoints
                .getFarthestFromPlayers(
                    players
                        .map { it.player }
                        .filter {
                            it != player && it.isOnline && it.gameMode != GameMode.SPECTATOR
                        },
                    brawlWorld!!.world,
                ) ?: SpawnPoint(0.0, 100.0, 0.0)

        player.teleport(spawnPoint)
        player.feed()
        player.heal()
        player.gameMode = GameMode.SURVIVAL

        // Handle kit switching on respawn for both ON_DEATH and IMMEDIATE modes
        // (IMMEDIATE mode needs this for cases where kit was selected during spectator countdown)
        if (
            minigameData.kitSwitchingMode == KitSwitchingMode.ON_DEATH ||
                minigameData.kitSwitchingMode == KitSwitchingMode.IMMEDIATE
        ) {
            val currentKit = kitService.getKitForPlayer(player)
            val selectedKit = kitService.currentSelectedKitForPlayer(player)

            if (currentKit?.id != selectedKit.id) {
                // Unassign current kit and assign the selected one
                kitService.unassignKit(player)
            }
        }

        assignPlayerKit(player)
    }

    fun isPlayerInMinigame(player: Player): Boolean {
        return players.find { it.player == player } != null &&
            !disconnectedPlayers.contains(player.uniqueId)
    }

    open fun canPlayerLeaveMinigame(player: Player): Boolean {
        return true
    }

    open fun onPlayerLeave(player: Player) {
        kitService.unassignKit(player)
        assignedKits.removeIf { it.first == player }
        if (!disconnectedPlayers.contains(player.uniqueId)) {
            disconnectedPlayers.add(player.uniqueId)
        }
        // Clean up respawning state if player leaves while respawning
        respawningPlayers.remove(player.uniqueId)
    }

    /** Called when a player disconnects from the server while in this minigame */
    open fun onPlayerDisconnect(player: Player) {
        if (!isPlayerInMinigame(player)) return

        disconnectedPlayers.add(player.uniqueId)
        onPlayerLeave(player)
        checkAndHandleMinigameEnd()
    }

    /**
     * Called when a player rejoins the server Returns true if the player was teleported back to the
     * minigame
     */
    open fun onPlayerReconnect(player: Player): Boolean {
        // Only handle if player was previously in this minigame and disconnected
        if (!disconnectedPlayers.remove(player.uniqueId) || !minigameData.allowRejoinAfterLeave) {
            return false
        }

        // Only teleport back if minigame is still active and not ended
        if (state == MinigameState.ENDED) {
            return false
        }

        // Add player back to the minigame
        // Note: We need to restore the player to the active players list
        // This assumes the MinigamePlayer was not removed from the original players list
        if (!isPlayerInMinigame(player)) {
            return false
        }

        // Teleport player back to the minigame world
        if (brawlWorld != null) {
            val spawnPoint =
                brawlWorld!!
                    .data
                    .spawnPoints
                    .getFarthestFromPlayers(
                        players.filter { it.player != player }.map { it.player },
                        brawlWorld!!.world,
                    ) ?: brawlWorld!!.data.spawnPoints.firstOrNull() ?: return false

            player.teleport(brawlWorld!!.world.location(spawnPoint))
            assignPlayerKit(player)
            player.gameMode = GameMode.SURVIVAL
        }

        return true
    }

    /**
     * Checks if the minigame should end due to insufficient players and takes appropriate action
     */
    protected open fun checkAndHandleMinigameEnd() {
        if (state == MinigameState.ENDED) return

        // Count active players (not spectator mode, still connected, and not disconnected)
        val activePlayers =
            players.filter { player ->
                val bukkitPlayer = player.player
                bukkitPlayer.isOnline &&
                    !disconnectedPlayers.contains(bukkitPlayer.uniqueId) &&
                    bukkitPlayer.gameMode != GameMode.SPECTATOR
            }

        // End minigame if no active players remain
        if (activePlayers.isEmpty()) {
            state = MinigameState.ENDED
            teardown()
        }
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
        if (brawlWorld == null) {
            return Err(
                RuntimeException("This function was called before brawlWorld finished loading")
            )
        }

        val voidLevel = brawlWorld!!.data.voidLevel

        runnables.add(
            repeatingTask(5) {
                players.forEach { player ->
                    if (
                        player.player.gameMode == GameMode.SURVIVAL &&
                            player.player.location.y <= voidLevel
                    ) {
                        plugin.logger.info("Player $player fell into the void")
                        BrawlDeathEvent.call(player.player, DeathReason.Void)
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
