package dev.betrix.superSmashMobsBrawl.services

import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.config.HubConfig
import dev.betrix.superSmashMobsBrawl.passives.definitions.DoubleJumpPassiveDefinition
import dev.betrix.superSmashMobsBrawl.passives.instances.PassiveInstance
import gg.flyte.twilight.event.event
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.java.JavaPlugin

/**
 * Service responsible for managing hub worlds and player hub interactions
 */
object HubService {
    private lateinit var plugin: JavaPlugin
    private val hubWorlds = mutableMapOf<String, HubWorld>()
    private val playersInHub = mutableSetOf<Player>()
    private val playerPassives = mutableMapOf<Player, PassiveInstance>()
    
    /**
     * Initialize the hub service
     */
    fun initialize(plugin: JavaPlugin) {
        this.plugin = plugin
        HubConfig.initialize(plugin)
        setupDefaultHub()
        registerEvents()
    }
    
    /**
     * Set up the default blue_forest hub
     */
    private fun setupDefaultHub() {
        val worldName = HubConfig.getDefaultWorldName()
        val world = plugin.server.getWorld(worldName)
        if (world != null) {
            val spawnLocation = HubConfig.getSpawnLocation(world)
            val hubWorld = HubWorld(worldName, world, spawnLocation)
            hubWorlds[worldName] = hubWorld
            plugin.logger.info("Hub world '$worldName' registered with spawn at ${spawnLocation.x}, ${spawnLocation.y}, ${spawnLocation.z}")
        } else {
            plugin.logger.warning("World '$worldName' not found! Hub system may not work properly.")
        }
    }
    
    /**
     * Register all hub-related events
     */
    private fun registerEvents() {
        // Player join event - teleport to hub and give double jump
        event<PlayerJoinEvent> {
            val hubWorld = getDefaultHub()
            if (hubWorld != null) {
                teleportToHub(player, hubWorld)
                giveHubPassives(player)
            }
        }
        
        // Player quit event - cleanup
        event<PlayerQuitEvent> {
            removeHubPassives(player)
            playersInHub.remove(player)
        }
        
        // Player teleport event - handle hub entry/exit
        event<PlayerTeleportEvent> {
            val fromHub = isInHub(player, from.world)
            val toHub = isInHub(player, to.world)
            
            if (!fromHub && toHub) {
                // Player entering hub
                playersInHub.add(player)
                giveHubPassives(player)
            } else if (fromHub && !toHub) {
                // Player leaving hub
                playersInHub.remove(player)
                removeHubPassives(player)
            }
        }
        
        // Damage protection in hub worlds
        event<EntityDamageEvent> {
            if (entity is Player) {
                val player = entity as Player
                if (isInHub(player, player.world)) {
                    isCancelled = true
                }
            }
        }
    }
    
    /**
     * Teleport a player to a specific hub
     */
    fun teleportToHub(player: Player, hubWorld: HubWorld) {
        player.teleport(hubWorld.spawnLocation)
        player.gameMode = GameMode.ADVENTURE
        player.fallDistance = 0f
        playersInHub.add(player)
        giveHubPassives(player)
    }
    
    /**
     * Teleport a player to the default hub
     */
    fun teleportToDefaultHub(player: Player) {
        val hubWorld = getDefaultHub()
        if (hubWorld != null) {
            teleportToHub(player, hubWorld)
        }
    }
    
    /**
     * Check if a player is in a hub world
     */
    fun isInHub(player: Player?, world: World?): Boolean {
        return world != null && hubWorlds.values.any { it.world == world }
    }
    
    /**
     * Check if a player is currently in any hub
     */
    fun isPlayerInHub(player: Player): Boolean {
        return playersInHub.contains(player)
    }
    
    /**
     * Get the default hub world
     */
    fun getDefaultHub(): HubWorld? {
        return hubWorlds[HubConfig.getDefaultWorldName()]
    }
    
    /**
     * Get all registered hub worlds
     */
    fun getHubWorlds(): Map<String, HubWorld> {
        return hubWorlds.toMap()
    }
    
    /**
     * Register a new hub world
     */
    fun registerHubWorld(id: String, hubWorld: HubWorld) {
        hubWorlds[id] = hubWorld
        plugin.logger.info("Hub world '$id' registered")
    }
    
    /**
     * Give hub-specific passives to a player
     */
    private fun giveHubPassives(player: Player) {
        if (playerPassives.containsKey(player)) {
            return // Already has passives
        }
        
        // Give double jump passive if enabled
        if (HubConfig.isDoubleJumpEnabled()) {
            val doubleJumpPassive = DoubleJumpPassiveDefinition.createInstance(player)
            doubleJumpPassive.setup()
            playerPassives[player] = doubleJumpPassive
            plugin.logger.info("Gave double jump passive to player: ${player.name}")
        }
    }
    
    /**
     * Remove hub-specific passives from a player
     */
    private fun removeHubPassives(player: Player) {
        val passive = playerPassives.remove(player)
        passive?.teardown()
        
        plugin.logger.info("Removed hub passives from player: ${player.name}")
    }
    
    /**
     * Clean up all player data (called on plugin disable)
     */
    fun cleanup() {
        playerPassives.values.forEach { it.teardown() }
        playerPassives.clear()
        playersInHub.clear()
        plugin.logger.info("Hub service cleaned up")
    }
}

/**
 * Represents a hub world with its configuration
 */
data class HubWorld(
    val id: String,
    val world: World,
    val spawnLocation: Location
)