package dev.betrix.superSmashMobsBrawl.services

import com.github.michaelbull.result.*
import com.github.shynixn.mccoroutine.bukkit.launch
import dev.betrix.superSmashMobsBrawl.kits.BrawlKit
import dev.betrix.superSmashMobsBrawl.models.BrawlHubWorld
import dev.betrix.superSmashMobsBrawl.utils.createLocation
import gg.flyte.twilight.event.event
import gg.flyte.twilight.extension.feed
import gg.flyte.twilight.extension.heal
import gg.flyte.twilight.extension.removeActivePotionEffects
import gg.flyte.twilight.extension.resetFlySpeed
import gg.flyte.twilight.extension.resetWalkSpeed
import java.util.UUID
import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Service responsible for managing hub worlds and player hub interactions */
object HubService : KoinComponent {
    private lateinit var defaultHubWorld: BrawlHubWorld
    private lateinit var plugin: JavaPlugin
    private val dataService: DataService by inject()
    private val lang: LangService by inject()
    private val playersInHub = mutableSetOf<Player>()
    private val playerHubKits = mutableMapOf<Player, BrawlKit>()

    fun teardown() {
        playerHubKits.values.forEach { it.teardown() }
        playerHubKits.clear()
        playersInHub.clear()
        plugin.logger.info("Hub service cleaned up")
    }

    fun initialize(plugin: JavaPlugin) {
        this.plugin = plugin

        val defaultHubId = UUID.randomUUID().toString()
        plugin.logger.info("Trying to load default world hub with id $defaultHubId")

        plugin.launch {
            dataService.awaitReady()

            val mapDef = dataService.getHubMap("blue_forest")!!

            WorldService.copyAndLoadWorld(mapDef)
                .mapBoth(
                    success = { loadedWorld ->
                        val hub = loadedWorld as? BrawlHubWorld
                        if (hub == null) {
                            plugin.logger.severe("Loaded world is not a hub world!")
                            return@mapBoth
                        }
                        plugin.logger.info("Successfully loaded default hub world")
                        defaultHubWorld = hub
                        registerEvents()
                    },
                    failure = { err ->
                        plugin.logger.severe("Couldn't load default hub")
                        err.printStackTrace()
                    },
                )
        }
    }

    fun playersInHubCount(): Int {
        return playersInHub.size
    }

    private fun registerEvents() {
        event<PlayerJoinEvent> {
            plugin.logger.info("${player.name} joined")

            joinMessage(lang.t("messages.players.joinServer") { "playerName" to player.name })

            if (!::defaultHubWorld.isInitialized) {
                plugin.logger.severe("No default hub world set")
                player.kick(lang.t("messages.kick.serverStarting"))
                return@event
            }

            teleportToHub(player, defaultHubWorld).onFailure {
                player.kick(lang.t("messages.kick.serverStarting"))
                return@event
            }
            giveHubPassives(player)
        }

        event<PlayerQuitEvent> {
            removeHubPassives(player)
            playersInHub.remove(player)
        }

        event<PlayerTeleportEvent> {
            val fromHub = isWorldHub(from.world)
            val toHub = isWorldHub(to.world)

            if (!fromHub && toHub) {
                playersInHub.add(player)
                giveHubPassives(player)
            } else if (fromHub && !toHub) {
                playersInHub.remove(player)
                removeHubPassives(player)
            }
        }

        event<EntityDamageEvent> {
            if (entity is Player) {
                val player = entity as Player
                if (isPlayerInHub(player)) {
                    isCancelled = true
                }
            }
        }
    }

    fun teleportToHub(player: Player, hubWorld: BrawlHubWorld): Result<Unit, Exception> {
        if (hubWorld.data.spawnPoints.isEmpty()) {
            return Err(RuntimeException("Hub spawn points were empty"))
        }

        val spawnPoint = hubWorld.data.spawnPoints[0]
        val teleportSuccess = player.teleport(createLocation(hubWorld.world, spawnPoint))

        if (!teleportSuccess) {
            return Err(RuntimeException("Teleport to hub failed for ${player.name}"))
        }

        player.inventory.clear()
        player.feed()
        player.heal()
        player.resetWalkSpeed()
        player.resetFlySpeed()
        player.gameMode = GameMode.ADVENTURE
        player.fallDistance = 0f
        player.removeActivePotionEffects()
        playersInHub.add(player)

        return Ok(Unit)
    }

    fun teleportToDefaultHub(player: Player): Result<Unit, Exception> {
        if (!::defaultHubWorld.isInitialized) {
            return Err(IllegalStateException("Default hub world is not yet initialized"))
        }
        return teleportToHub(player, defaultHubWorld)
    }

    fun tryTeleportToDefaultHub(player: Player): Result<Unit, Exception> {
        return if (::defaultHubWorld.isInitialized) {
            try {
                teleportToHub(player, defaultHubWorld)
            } catch (e: Exception) {
                Err(e)
            }
        } else {
            Err(IllegalStateException("Default hub world is not yet initialized"))
        }
    }

    fun isPlayerInHub(player: Player): Boolean {
        return isWorldHub(player.world)
    }

    fun isWorldHub(world: World): Boolean =
        ::defaultHubWorld.isInitialized && defaultHubWorld.world == world

    private fun giveHubPassives(player: Player) {
        if (playerHubKits.containsKey(player)) {
            return
        }

        val hubKit = BrawlKit("hub", player)
        hubKit.setup()

        playerHubKits[player] = hubKit
    }

    private fun removeHubPassives(player: Player) {
        val hubKit = playerHubKits.remove(player)
        hubKit?.teardown()

        plugin.logger.info("Removed hub passives from player: ${player.name}")
    }
}
