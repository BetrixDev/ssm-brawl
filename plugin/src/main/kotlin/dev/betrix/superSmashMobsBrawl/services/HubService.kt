package dev.betrix.superSmashMobsBrawl.services

import com.github.michaelbull.result.mapBoth
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.github.shynixn.mccoroutine.bukkit.launch
import dev.betrix.superSmashMobsBrawl.maps.SsmbMap
import dev.betrix.superSmashMobsBrawl.maps.blueForestHub
import dev.betrix.superSmashMobsBrawl.kits.definitions.CreeperKitDefinition
import dev.betrix.superSmashMobsBrawl.registries.MapRegistry
import dev.betrix.superSmashMobsBrawl.services.KitService
import dev.betrix.superSmashMobsBrawl.utils.createLocation
import gg.flyte.twilight.event.event
import java.util.UUID
import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.java.JavaPlugin

/** Service responsible for managing hub worlds and player hub interactions */
object HubService {
    private lateinit var defaultLoadedHubWorld: LoadedWorld
    private lateinit var plugin: JavaPlugin
    private val playersInHub = mutableSetOf<Player>()

    /** Removes all current loaded worlds and teleports all players to default world */
    fun teardown() {
        // Clean up all players in hub by removing their kits
        playersInHub.forEach { player ->
            KitService.unassignKit(player)
        }
        playersInHub.clear()
        plugin.logger.info("Hub service cleaned up")
    }

    /** Initialize the hub service */
    fun initialize(plugin: JavaPlugin) {
        this.plugin = plugin

        val defaultHubId = UUID.randomUUID().toString()

        plugin.logger.info("Trying to load default world hub with id $defaultHubId")

        plugin.launch {
            WorldService.copyAndLoadWorld(blueForestHub)
                .mapBoth(
                    success = { loadedWorld ->
                        plugin.logger.info("Successfully loaded default hub world")
                        defaultLoadedHubWorld = loadedWorld
                        registerEvents()
                    },
                    failure = { err ->
                        plugin.logger.severe("Couldn't load default hub")
                        err.printStackTrace()
                    },
                )
        }
    }

    /** Register all hub-related events */
    private fun registerEvents() {
        // Player join event - teleport to hub and give double jump
        event<PlayerJoinEvent> {
            plugin.logger.info("${player.name} joined in hub server right now")

            if (!::defaultLoadedHubWorld.isInitialized) {
                plugin.logger.severe("No default hub world set")
                return@event
            }

            player.inventory.clear()
            teleportToHub(player, defaultLoadedHubWorld)
            giveHubPassives(player)
            plugin.logger.info("Player ${player.name} has been set up in hub with passives")
        }

        // Player quit event - cleanup
        event<PlayerQuitEvent> {
            removeHubPassives(player)
            playersInHub.remove(player)
        }

        // Player teleport event - handle hub entry/exit
        event<PlayerTeleportEvent> {
            val fromHub = isWorldHub(from.world)
            val toHub = isWorldHub(to.world)

            if (!fromHub && toHub) {
                // Player entering hub
                plugin.logger.info("Player ${player.name} entering hub from ${from.world.name} to ${to.world.name}")
                player.inventory.clear()
                playersInHub.add(player)
                giveHubPassives(player)
            } else if (fromHub && !toHub) {
                // Player leaving hub
                plugin.logger.info("Player ${player.name} leaving hub from ${from.world.name} to ${to.world.name}")
                playersInHub.remove(player)
                removeHubPassives(player)
            }
        }

        // Damage protection in hub worlds
        event<EntityDamageEvent> {
            if (entity is Player) {
                val player = entity as Player
                if (isPlayerInHub(player)) {
                    isCancelled = true
                }
            }
        }
    }

    /** Teleport a player to a specific hub */
    fun teleportToHub(player: Player, loadedWorld: LoadedWorld) {
        if (loadedWorld.mapDefinition.spawnPoints.isNotEmpty()) {
            val spawnPoint = loadedWorld.mapDefinition.spawnPoints[0]
            player.teleport(createLocation(loadedWorld.world, spawnPoint))
            player.gameMode = GameMode.ADVENTURE
            player.fallDistance = 0f
            playersInHub.add(player)
        }
    }

    /** Teleport a player to the default hub */
    fun teleportToDefaultHub(player: Player) {
        if (!::defaultLoadedHubWorld.isInitialized) {
            throw IllegalStateException("Default hub world is not yet initialized")
        }
        teleportToHub(player, defaultLoadedHubWorld)
    }

    /** Teleport a player to the default hub with result handling */
    fun tryTeleportToDefaultHub(player: Player): Result<Unit> {
        return if (::defaultLoadedHubWorld.isInitialized) {
            try {
                teleportToHub(player, defaultLoadedHubWorld)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            Result.failure(IllegalStateException("Default hub world is not yet initialized"))
        }
    }

    /** Check if a player is in a hub world */
    fun isPlayerInHub(player: Player): Boolean {
        return isWorldHub(player.world)
    }

    fun isWorldHub(world: World): Boolean = defaultLoadedHubWorld.world == world

    /** Get the default hub world */
    fun getDefaultHub(): SsmbMap? {
        return MapRegistry.getDefaultHub()
    }

    /** Get all registered hub worlds */
    fun getHubWorlds(): List<SsmbMap> {
        return MapRegistry.getHubMaps()
    }

    /** Give hub-specific passives to a player using the proper kit system */
    private fun giveHubPassives(player: Player) {
        // If player already has a kit, unassign it first to ensure clean state
        if (KitService.hasKit(player)) {
            KitService.unassignKit(player)
            plugin.logger.info("Unassigned existing kit from player ${player.name} for hub transition")
        }
        
        // Use the default kit (CreeperKit) which includes DoubleJumpPassive
        KitService.assignKit(player, CreeperKitDefinition)
            .onFailure { error ->
                plugin.logger.warning("Failed to assign hub kit to player ${player.name}: $error")
            }
            .onSuccess {
                plugin.logger.info("Gave hub kit (with double jump) to player: ${player.name}")
            }
    }

    /** Remove hub-specific passives from a player */
    private fun removeHubPassives(player: Player) {
        val removedKit = KitService.unassignKit(player)
        if (removedKit != null) {
            plugin.logger.info("Removed hub kit from player: ${player.name}")
        }
    }
}
