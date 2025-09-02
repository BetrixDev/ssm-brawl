package dev.betrix.superSmashMobsBrawl.minigames

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.github.michaelbull.result.unwrap
import com.github.quillraven.fleks.World
import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.components.DeadComponent
import dev.betrix.superSmashMobsBrawl.components.InMinigameComponent
import dev.betrix.superSmashMobsBrawl.components.RespawnComponent
import dev.betrix.superSmashMobsBrawl.events.BrawlDeathEvent
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.events.DeathReason
import dev.betrix.superSmashMobsBrawl.events.PlayerSelectKitEvent
import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent
import dev.betrix.superSmashMobsBrawl.extensions.doKnockback
import dev.betrix.superSmashMobsBrawl.extensions.ecsEntity
import dev.betrix.superSmashMobsBrawl.extensions.getEquidistant
import dev.betrix.superSmashMobsBrawl.extensions.getFarthestFromPlayers
import dev.betrix.superSmashMobsBrawl.extensions.location
import dev.betrix.superSmashMobsBrawl.extensions.teleport
import dev.betrix.superSmashMobsBrawl.kits.BrawlKit
import dev.betrix.superSmashMobsBrawl.models.BrawlGameWorld
import dev.betrix.superSmashMobsBrawl.models.MinigamePlayer
import dev.betrix.superSmashMobsBrawl.models.MinigameState
import dev.betrix.superSmashMobsBrawl.models.brawlData.KitSwitchingMode
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import dev.betrix.superSmashMobsBrawl.services.*
import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.repeatingTask
import java.util.UUID
import kotlin.math.min
import kotlinx.coroutines.delay
import org.bukkit.GameMode
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
    private val ecsWorld: World by inject()

    @Suppress("UNCHECKED_CAST")
    protected val minigameData: TMinigameDef by lazy {
        (dataService.getMinigame(minigameId)
            ?: throw RuntimeException("Could not find minigame data for id $minigameId"))
            as TMinigameDef
    }

    var brawlWorld: BrawlGameWorld? = null
        protected set

    protected val assignedKits = mutableListOf<Pair<Player, BrawlKit>>()

    var state = MinigameState.PREFLIGHT
        protected set

    /** Tracks players who disconnected while in this minigame */
    protected val disconnectedPlayers = mutableSetOf<UUID>()

    // Respawn state is tracked via ECS RespawnComponent

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
        val entity = player.ecsEntity ?: return false
        return with(ecsWorld) { entity.has(RespawnComponent) }
    }

    open suspend fun initMinigame(): Result<Unit, Exception> {
        listeners.add(
            BrawlDeathEvent.listen(this) {
                val ecsEntity = player.ecsEntity ?: return@listen
                with(ecsWorld) {
                    // Avoid double-processing if already in death/respawn flow
                    if (ecsEntity.has(DeadComponent) || ecsEntity.has(RespawnComponent)) {
                        return@with
                    }
                    ecsEntity.configure { it += DeadComponent(reason) }
                }
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
            // Mark player as inside this minigame in ECS
            val ecsEntity = player.player.ecsEntity
            if (ecsEntity != null) {
                with(ecsWorld) {
                    ecsEntity.configure { it += InMinigameComponent(this@BrawlMinigame) }
                }
            }
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

    open fun decideDeath(player: Player, reason: DeathReason): DeathDecision {
        val delay = minigameData.respawnDelaySeconds
        return DeathDecision.Respawn(delaySeconds = delay)
    }

    open fun onPostDeathProcessed(player: Player, decision: DeathDecision) {}

    fun isPlayerInMinigame(player: Player): Boolean {
        return players.find { it.player == player } != null &&
            !disconnectedPlayers.contains(player.uniqueId)
    }

    open fun canPlayerLeaveMinigame(player: Player): Boolean {
        return true
    }

    open fun onPlayerLeave(player: Player) {
        unassignPlayerKit(player)
        if (!disconnectedPlayers.contains(player.uniqueId)) {
            disconnectedPlayers.add(player.uniqueId)
        }
        // Remove ECS minigame marker
        val ecsEntity = player.ecsEntity
        if (ecsEntity != null) {
            with(ecsWorld) {
                ecsEntity.configure {
                    it -= InMinigameComponent
                    it -= RespawnComponent
                    it -= DeadComponent
                }
            }
        }
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

    fun assignPlayerKit(player: Player) {
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

    fun unassignPlayerKit(player: Player) {
        kitService.unassignKit(player)?.let { assignedKits.removeIf { it.first == player } }
    }

    fun handleKitSwitchOnRespawn(player: Player) {
        if (
            minigameData.kitSwitchingMode == KitSwitchingMode.ON_DEATH ||
                minigameData.kitSwitchingMode == KitSwitchingMode.IMMEDIATE
        ) {
            val currentKit = kitService.getKitForPlayer(player)
            val selectedKit = kitService.currentSelectedKitForPlayer(player)

            if (currentKit?.id != selectedKit.id) {
                kitService.unassignKit(player)
            }
        }
    }

    fun getParticipants(): List<Player> = players.map { it.player }

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
