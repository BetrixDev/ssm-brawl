package dev.betrix.superSmashMobsBrawl.minigames.instances

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent
import dev.betrix.superSmashMobsBrawl.extensions.getEquidistant
import dev.betrix.superSmashMobsBrawl.maps.SpawnPoint
import dev.betrix.superSmashMobsBrawl.minigames.MinigameState
import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import dev.betrix.superSmashMobsBrawl.models.MinigameTeam
import dev.betrix.superSmashMobsBrawl.services.AssignKitError
import dev.betrix.superSmashMobsBrawl.services.KitService
import dev.betrix.superSmashMobsBrawl.utils.createLocation
import dev.betrix.superSmashMobsBrawl.utils.mm
import gg.flyte.twilight.event.event
import gg.flyte.twilight.extension.feed
import gg.flyte.twilight.extension.heal
import gg.flyte.twilight.extension.resetFlySpeed
import gg.flyte.twilight.extension.resetWalkSpeed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.util.Vector
import java.time.Duration
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.seconds

class TwoPlayerDuelsMinigameInstance(definition: MinigameDefinition, teams: List<MinigameTeam>): MinigameInstance(definition, teams) {
    private val plugin = SuperSmashMobsBrawl.instance

    private val countdownSeconds = 5
    private val deathSpectatorSeconds = 5
    private val spectatorHeightAboveSpawn = 50.0
    
    // Track dead players to prevent multiple death events
    private val deadPlayers = mutableSetOf<Player>()

    override suspend fun initMinigame(): Result<Unit, Exception> {
        super.initMinigame().onFailure { return Err(it) }

        val spawnPoints = map.spawnPoints.getEquidistant(players.size)

        teams.forEachIndexed { teamIdx, team ->
            team.players.forEachIndexed { playerIdx, player  ->
                player.feed()
                player.heal()
                player.gameMode = GameMode.SURVIVAL

                // Freeze player
                player.walkSpeed = 0f
                player.flySpeed = 0f
                player.velocity = Vector(0, 0, 0)

                player.teleport(createLocation(world, spawnPoints[(teamIdx + 1) * playerIdx]))

                KitService.assignKit(player).onFailure {
                    when (it) {
                        AssignKitError.PLAYER_HAS_KIT -> return Err(RuntimeException("Player $player already has a kit assigned to them"))
                    }
                }
            }
        }

        setupEventListeners()

        state = MinigameState.STARTING

        doCountdown()

        players.forEach { player ->
            player.resetWalkSpeed()
        }

        state = MinigameState.ONGOING

        return Ok(Unit)
    }

    override suspend fun teardownMinigame() {
        super.teardownMinigame()

        players.forEach { player ->
            player.resetFlySpeed()
            player.resetWalkSpeed()
            KitService.unassignKit(player)
        }
        
        // Clear dead players set
        deadPlayers.clear()
    }

    override fun shouldEndMinigame(): Boolean {
        teams.forEach {
            if (it.players.isEmpty()) {
                return true
            }
        }

        return false
    }

    override suspend fun onMinigameEnd() {
        val winningTeam = teams.first { it.stocks > 0 }

        winningTeam.players.forEach {
            it.sendMessage("You won!")
        }

        super.onMinigameEnd()
    }

    private suspend fun doCountdown() {
        withContext(plugin.minecraftDispatcher) {
            repeat(countdownSeconds) { iteration ->
                playPingSound()

                val title = Title.title(
                    mm("<green>${iteration + 1}</green>"),
                    mm("<gray>Game starting in</gray>"),
                    Title.Times.times(
                        Duration.ofMillis(250),
                        Duration.ofMillis(500),
                        Duration.ofMillis(250),
                    )
                )

                world.showTitle(title)

                withContext(Dispatchers.IO) {
                    delay(1.seconds)
                }
            }

            playPingSound()

            val startTitle = Title.title(
                mm("<green>Go!</green>"),
                Component.empty()
            )

            world.showTitle(startTitle)
        }
    }

    private fun playPingSound() {
        players.forEach { player ->
            player.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
        }
    }
    
    private fun setupEventListeners() {
        // Listen for SmashDamageEvent to apply damage and handle deaths
        event<SmashDamageEvent> {
            if (!isValid(this@TwoPlayerDuelsMinigameInstance)) return@event
            if (state != MinigameState.ONGOING) return@event
            
            val player = victim as? Player ?: return@event
            if (deadPlayers.contains(player)) return@event
            
            // Apply damage to the player
            val newHealth = (player.health - damage).coerceAtLeast(0.0)
            player.health = newHealth
            
            // Apply knockback
            if (knockbackMultiplier > 0.0 && damager is dev.betrix.superSmashMobsBrawl.events.Damager.LivingEntity) {
                val damagerEntity = damager.livingEntity
                val direction = player.location.toVector().subtract(damagerEntity.location.toVector()).normalize()
                val knockback = direction.multiply(knockbackMultiplier)
                player.velocity = player.velocity.add(knockback)
            }
            
            // Check if player should die
            if (newHealth <= 0.0) {
                handlePlayerDeath(player)
            }
        }
        
        // Listen for PlayerDeathEvent to handle the death loop
        event<PlayerDeathEvent> {
            val player = player
            if (!isPlayerInMinigame(player)) return@event
            if (state != MinigameState.ONGOING) return@event
            
            // Cancel the default death behavior
            isCancelled = true
            
            handlePlayerDeath(player)
        }
    }
    
    private fun handlePlayerDeath(player: Player) {
        if (deadPlayers.contains(player)) return
        deadPlayers.add(player)
        
        // Find the team this player belongs to
        val playerTeam = teams.find { it.players.contains(player) } ?: return
        
        // Reduce team stocks
        playerTeam.stocks--
        
        // Set player to spectator mode
        player.gameMode = GameMode.SPECTATOR
        
        // Remove kit and clear inventory
        KitService.unassignKit(player)
        player.inventory.clear()
        
        // Teleport to spectator position (50 blocks above a random spawn point)
        val randomSpawnPoint = map.spawnPoints.random()
        val spectatorLocation = createLocation(world, randomSpawnPoint).apply {
            y += spectatorHeightAboveSpawn
        }
        player.teleport(spectatorLocation)
        
        // Send death message
        player.sendMessage(mm("<red>You died! Respawning in $deathSpectatorSeconds seconds...</red>"))
        
        // Check if game should end after stock reduction
        if (shouldEndMinigame()) {
            SuperSmashMobsBrawl.instance.launch {
                state = MinigameState.ENDED
                onMinigameEnd()
                teardownMinigame()
            }
            return
        }
        
        // Start respawn timer
        SuperSmashMobsBrawl.instance.launch {
            withContext(plugin.minecraftDispatcher) {
                delay(deathSpectatorSeconds.seconds)
                
                // Only respawn if player is still in the minigame and game is ongoing
                if (isPlayerInMinigame(player) && state == MinigameState.ONGOING) {
                    respawnPlayer(player)
                }
            }
        }
    }
    
    private suspend fun respawnPlayer(player: Player) {
        // Remove from dead players set
        deadPlayers.remove(player)
        
        // Find the furthest spawn point from enemy players
        val furthestSpawnPoint = findFurthestSpawnPointFromEnemies(player)
        
        // Teleport to respawn location
        player.teleport(createLocation(world, furthestSpawnPoint))
        
        // Reset player state
        player.gameMode = GameMode.SURVIVAL
        @Suppress("DEPRECATION")
        player.health = player.maxHealth
        player.feed()
        player.heal()
        
        // Reassign kit
        KitService.assignKit(player)
        
        // Send respawn message
        player.sendMessage(mm("<green>You have respawned!</green>"))
    }
    
    private fun findFurthestSpawnPointFromEnemies(respawningPlayer: Player): SpawnPoint {
        // Get all enemy players (players not on the same team)
        val respawningPlayerTeam = teams.find { it.players.contains(respawningPlayer) }
        val enemyPlayers = teams.filter { it != respawningPlayerTeam }
            .flatMap { it.players }
            .filter { !deadPlayers.contains(it) } // Only consider alive enemies
        
        if (enemyPlayers.isEmpty()) {
            // If no enemies are alive, return any spawn point
            return map.spawnPoints.random()
        }
        
        // Calculate the spawn point with maximum distance from all enemies
        return map.spawnPoints.maxByOrNull { spawnPoint ->
            val spawnLocation = createLocation(world, spawnPoint)
            enemyPlayers.minOfOrNull { enemy ->
                calculateDistance(spawnLocation, enemy.location)
            } ?: Double.MAX_VALUE
        } ?: map.spawnPoints.random()
    }
    
    private fun calculateDistance(loc1: Location, loc2: Location): Double {
        val deltaX = loc1.x - loc2.x
        val deltaY = loc1.y - loc2.y
        val deltaZ = loc1.z - loc2.z
        return sqrt(deltaX.pow(2) + deltaY.pow(2) + deltaZ.pow(2))
    }
}