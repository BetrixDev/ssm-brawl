package dev.betrix.superSmashMobsBrawl.services

import com.github.michaelbull.result.mapBoth
import com.github.shynixn.mccoroutine.bukkit.launch
import dev.betrix.superSmashMobsBrawl.di.Injectable
import dev.betrix.superSmashMobsBrawl.lifecycle.Manageable
import dev.betrix.superSmashMobsBrawl.maps.SsmbMap
import dev.betrix.superSmashMobsBrawl.maps.blueForestHub
import dev.betrix.superSmashMobsBrawl.passives.definitions.DoubleJumpPassiveDefinition
import dev.betrix.superSmashMobsBrawl.passives.instances.PassiveInstance
import dev.betrix.superSmashMobsBrawl.registries.MapRegistry
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
import org.koin.core.component.inject

/** Service responsible for managing hub worlds and player hub interactions */
object HubService : Manageable, Injectable {
    private lateinit var defaultLoadedHubWorld: LoadedWorld
    private val plugin: JavaPlugin by inject()
    private val playersInHub = mutableSetOf<Player>()
    private val playerPassives = mutableMapOf<Player, PassiveInstance>()

    /** Removes all current loaded worlds and teleports all players to default world */
    override fun teardown() {
        playerPassives.values.forEach { it.teardown() }
        playerPassives.clear()
        playersInHub.clear()
        plugin.logger.info("Hub service cleaned up")
    }

    /** Initialize the hub service */
    fun initialize(plugin: JavaPlugin) {

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
                player.inventory.clear()
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

    /** Give hub-specific passives to a player */
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

    /** Remove hub-specific passives from a player */
    private fun removeHubPassives(player: Player) {
        val passive = playerPassives.remove(player)
        passive?.teardown()

        plugin.logger.info("Removed hub passives from player: ${player.name}")
    }
}
