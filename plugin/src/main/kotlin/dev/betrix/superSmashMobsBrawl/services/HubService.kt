package dev.betrix.superSmashMobsBrawl.services

import com.github.michaelbull.result.*
import com.github.shynixn.mccoroutine.bukkit.launch
import dev.betrix.superSmashMobsBrawl.brawl.BrawlPassive
import dev.betrix.superSmashMobsBrawl.brawl.registry.BrawlPassiveRegistry
import dev.betrix.superSmashMobsBrawl.maps.SsmbMap
import dev.betrix.superSmashMobsBrawl.maps.blueForestHub
import dev.betrix.superSmashMobsBrawl.models.BrawlHubWorld
import dev.betrix.superSmashMobsBrawl.models.brawlData.HubMapDef
import dev.betrix.superSmashMobsBrawl.passives.PassiveMetadata
import dev.betrix.superSmashMobsBrawl.registries.MapRegistry
import dev.betrix.superSmashMobsBrawl.utils.createLocation
import gg.flyte.twilight.event.event
import gg.flyte.twilight.extension.feed
import gg.flyte.twilight.extension.heal
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
    private val playerPassives = mutableMapOf<Player, BrawlPassive>()

    fun teardown() {
        playerPassives.values.forEach { it.teardown() }
        playerPassives.clear()
        playersInHub.clear()
        plugin.logger.info("Hub service cleaned up")
    }

    fun initialize(plugin: JavaPlugin) {
        this.plugin = plugin

        val defaultHubId = UUID.randomUUID().toString()
        plugin.logger.info("Trying to load default world hub with id $defaultHubId")

        plugin.launch {
            val hubMap: SsmbMap = MapRegistry.getDefaultHub() ?: blueForestHub
            val mapDef =
                HubMapDef(
                    id = hubMap.id,
                    worldBorderSize = hubMap.worldBorderSize,
                    voidLevel = hubMap.voidLevel.toDouble(),
                    creators = hubMap.creatorUuids,
                    spawnPoints =
                        hubMap.spawnPoints.map { sp ->
                            dev.betrix.superSmashMobsBrawl.models.SpawnPoint(
                                x = sp.position.x,
                                y = sp.position.y,
                                z = sp.position.z,
                                yaw = sp.yaw,
                                pitch = sp.pitch,
                            )
                        },
                )

            WorldService.copyAndLoadWorld<BrawlHubWorld>(mapDef)
                .mapBoth(
                    success = { loadedWorld ->
                        plugin.logger.info("Successfully loaded default hub world")
                        defaultHubWorld = loadedWorld
                        registerEvents()
                    },
                    failure = { err ->
                        plugin.logger.severe("Couldn't load default hub")
                        err.printStackTrace()
                    },
                )
        }
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
        player.teleport(createLocation(hubWorld.world, spawnPoint))
        player.inventory.clear()
        player.feed()
        player.heal()
        player.resetWalkSpeed()
        player.resetFlySpeed()
        player.gameMode = GameMode.SURVIVAL
        player.fallDistance = 0f
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

    fun getDefaultHub(): SsmbMap? {
        return MapRegistry.getDefaultHub()
    }

    fun getHubWorlds(): List<SsmbMap> {
        return MapRegistry.getHubMaps()
    }

    private fun giveHubPassives(player: Player) {
        if (playerPassives.containsKey(player)) {
            return
        }

        val passiveDef = dataService.getPassive("double_jump")
        val passiveMetadata = PassiveMetadata(userFacing = false)
        val passive =
            BrawlPassiveRegistry.create("double_jump", player, passiveMetadata, emptyMap())
        if (passive != null) {
            passive.setup()
            playerPassives[player] = passive
            plugin.logger.info("Gave double jump passive to player: ${player.name}")
        } else {
            plugin.logger.warning("Failed to create double jump passive for player: ${player.name}")
        }
    }

    private fun removeHubPassives(player: Player) {
        val passive = playerPassives.remove(player)
        passive?.teardown()

        plugin.logger.info("Removed hub passives from player: ${player.name}")
    }
}
