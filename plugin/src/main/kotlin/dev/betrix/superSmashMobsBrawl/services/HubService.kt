package dev.betrix.superSmashMobsBrawl.services

import dev.betrix.superSmashMobsBrawl.maps.SsmbMap
import dev.betrix.superSmashMobsBrawl.maps.blueForestHub
import dev.betrix.superSmashMobsBrawl.passives.definitions.DoubleJumpPassiveDefinition
import dev.betrix.superSmashMobsBrawl.passives.instances.PassiveInstance
import dev.betrix.superSmashMobsBrawl.registries.MapRegistry
import dev.betrix.superSmashMobsBrawl.utils.WorldUtils
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
import java.util.UUID

/**
 * Service responsible for managing hub worlds and player hub interactions
 */
object HubService {
    private lateinit var defaultHubWorld: Pair<World, SsmbMap>
    private lateinit var plugin: JavaPlugin
    private val playersInHub = mutableSetOf<Player>()
    private val playerPassives = mutableMapOf<Player, PassiveInstance>()
    
    /**
     * Initialize the hub service
     */
    fun initialize(plugin: JavaPlugin) {
        this.plugin = plugin

        val defaultHubId = UUID.randomUUID().toString()

        plugin.logger.info("Trying to load default world hub with id $defaultHubId")

        WorldUtils.copyAndLoadWorldAsync("blue_forest", defaultHubId) { result ->
            result.fold(
                onSuccess = { world ->
                    plugin.logger.info("Successfully loaded default hub world")
                    defaultHubWorld = Pair(world, blueForestHub)
                    registerEvents()
                },
                onFailure = { err ->
                    plugin.logger.severe("Couldn't load default hub")
                    err.printStackTrace()
                }
            )
        }
    }
    
    /**
     * Register all hub-related events
     */
    private fun registerEvents() {
        // Player join event - teleport to hub and give double jump
        event<PlayerJoinEvent> {
            plugin.logger.info("${player.name} joined in hub server right now")

            if (!::defaultHubWorld.isInitialized) {
                plugin.logger.severe("No default hub world set")
                return@event
            }

            teleportToHub(player, defaultHubWorld.first, defaultHubWorld.second)
            giveHubPassives(player)
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
    fun teleportToHub(player: Player, world: World, hubMap: SsmbMap) {
        if (hubMap.spawnPoints.isNotEmpty()) {
            val spawnPoint = hubMap.spawnPoints[0]
            val location = Location(world, spawnPoint.x, spawnPoint.y, spawnPoint.z)
            player.teleport(location)
            player.gameMode = GameMode.ADVENTURE
            player.fallDistance = 0f
            playersInHub.add(player)
            giveHubPassives(player)
        }
    }
    
    /**
     * Teleport a player to the default hub
     */
    fun teleportToDefaultHub(player: Player) {
        teleportToHub(player, defaultHubWorld.first, defaultHubWorld.second)
    }
    
    /**
     * Check if a player is in a hub world
     */
    fun isInHub(player: Player?, world: World?): Boolean {
        return world != null && MapRegistry.isHubMap(world.name)
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
    fun getDefaultHub(): SsmbMap? {
        return MapRegistry.getDefaultHub()
    }
    
    /**
     * Get all registered hub worlds
     */
    fun getHubWorlds(): List<SsmbMap> {
        return MapRegistry.getHubMaps()
    }
    
    /**
     * Give hub-specific passives to a player
     */
    private fun giveHubPassives(player: Player) {
        if (playerPassives.containsKey(player)) {
            return // Already has passives
        }
        
        // Give double jump passive
        val doubleJumpPassive = DoubleJumpPassiveDefinition.createInstance(player)
        doubleJumpPassive.setup()
        playerPassives[player] = doubleJumpPassive
        plugin.logger.info("Gave double jump passive to player: ${player.name}")
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

