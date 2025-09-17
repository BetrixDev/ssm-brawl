package dev.betrix.superSmashMobsBrawl.minigames

import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.events.MinigameEndAnalyticsEvent
import dev.betrix.superSmashMobsBrawl.events.PlayerJoinMinigameAnalyticsEvent
import dev.betrix.superSmashMobsBrawl.events.PlayerSelectKitEvent
import dev.betrix.superSmashMobsBrawl.extensions.getEquidistant
import dev.betrix.superSmashMobsBrawl.extensions.getFarthestFromPlayers
import dev.betrix.superSmashMobsBrawl.extensions.location
import dev.betrix.superSmashMobsBrawl.extensions.teleport
import dev.betrix.superSmashMobsBrawl.minigames.managers.DefaultCombatManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.DefaultCountdownManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.DefaultEnvironmentProtectionManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.DefaultHazardManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.DefaultPlayerConnectionManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.DefaultRespawnManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.DefaultScoreboardManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.DefaultTeamManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.DefaultWorldManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.FfaGameObjectiveManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.ICombatManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.ICountdownManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.IEnvironmentProtectionManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.IGameObjectiveManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.IHazardManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.IPlayerConnectionManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.IRespawnManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.IScoreboardManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.ITeamManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.IWorldManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.TeamBasedStocksObjectiveManager
import dev.betrix.superSmashMobsBrawl.models.SpawnPoint
import dev.betrix.superSmashMobsBrawl.models.brawlData.FfaMinigameDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.KitDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.KitSwitchingMode
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.TeamBasedStocksMinigameDef
import dev.betrix.superSmashMobsBrawl.services.KitService
import gg.flyte.twilight.event.event
import java.util.UUID
import org.bukkit.OfflinePlayer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class MinigameTeam(
    val players: List<OfflinePlayer>,
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val metadata: MutableMap<String, Any> = mutableMapOf(),
) {
    init {
        require(players.isNotEmpty()) { "A team must have at least one player." }
    }
}

enum class MinigameState {
    PREFLIGHT,
    STARTING,
    IN_PROGRESS,
    ENDING,
    ENDED,
}

interface ITeleportationManager {
    fun teleportTeamToStartingLocation(team: MinigameTeam)

    fun teleportPlayerToSpectatorArea(player: OfflinePlayer)

    fun teleportPlayerRandomSpawnPoint(player: OfflinePlayer)
}

class DefaultTeleportationManager(private val minigame: BrawlMinigame) :
    ITeleportationManager, KoinComponent {
    private val plugin: SuperSmashMobsBrawl by inject()

    override fun teleportTeamToStartingLocation(team: MinigameTeam) {
        val world = minigame.worldManager.getWorld() ?: return
        val spawnPoints = world.data.spawnPoints.getEquidistant(team.players.size)

        if (spawnPoints.isEmpty()) {
            plugin.logger.info(
                "World ${world.world.name} has no spawn points defined, teleporting players to spectator area."
            )

            team.players.forEach { player ->
                if (player.isOnline) {
                    player.player?.teleport(world.data.spectatorSpawnPoint)
                }
            }
            return
        }

        team.players.forEachIndexed { index, player ->
            if (player.isOnline) {
                val spawnPoint = spawnPoints.getOrNull(index) ?: spawnPoints.first()
                player.player?.teleport(world.world.location(spawnPoint))

                // Clear offhand to remove shield mechanics (1.8 feel)
                try {
                    player.player
                        ?.inventory
                        ?.setItemInOffHand(
                            org.bukkit.inventory.ItemStack.of(org.bukkit.Material.AIR)
                        )
                } catch (e: Exception) {
                    plugin.logger.fine("Offhand clear failed for ${player.name}: ${e.message}")
                }
            }
        }
    }

    override fun teleportPlayerToSpectatorArea(player: OfflinePlayer) {
        val world = minigame.worldManager.getWorld() ?: return
        if (player.isOnline) {
            player.player?.teleport(world.data.spectatorSpawnPoint)
        }
    }

    override fun teleportPlayerRandomSpawnPoint(player: OfflinePlayer) {
        val world = minigame.worldManager.getWorld() ?: return
        if (player.isOnline) {
            val bukkitPlayer = player.player ?: return
            val activePlayers =
                minigame
                    .allPlayers()
                    .mapNotNull { if (it.isOnline) it.player else null }
                    .filter { it != bukkitPlayer && it.gameMode != org.bukkit.GameMode.SPECTATOR }

            val spawnPoint =
                world.data.spawnPoints.getFarthestFromPlayers(activePlayers, world.world)
                    ?: world.data.spawnPoints.firstOrNull()
                    ?: run {
                        plugin.logger.warning(
                            "World ${world.world.name} has no spawn points defined, teleporting to world spawn."
                        )

                        SpawnPoint(
                            world.world.spawnLocation.x,
                            world.world.spawnLocation.y,
                            world.world.spawnLocation.z,
                        )
                    }

            bukkitPlayer.teleport(world.world.location(spawnPoint))
        }
    }
}

interface IKitHandler {
    fun assignKitToPlayer(player: OfflinePlayer)

    fun removeKitFromPlayer(player: OfflinePlayer)
}

/** Default kit handler that keeps the player's kit unchanged throughout the game. */
class DefaultKitHandler(private val minigame: BrawlMinigame) : IKitHandler, KoinComponent {
    private val kitService: KitService by inject()

    private val initialPlayerKits: Map<UUID, KitDef> =
        minigame.teams
            .flatMap { it.players }
            .associate { player ->
                player.uniqueId to kitService.currentSelectedKitForPlayer(player)
            }
            .toMap()

    override fun assignKitToPlayer(player: OfflinePlayer) {
        val kit = initialPlayerKits[player.uniqueId]

        if (kit != null) {
            kitService.assignKit(player, kit)
        } else {
            kitService.assignKit(player)
        }
    }

    override fun removeKitFromPlayer(player: OfflinePlayer) {
        kitService.unassignKit(player)
    }
}

/** Allows players to switch kits at anytime during the game */
class KitHandlerWithSwitching(private val minigame: BrawlMinigame) : IKitHandler, KoinComponent {
    private val kitService: KitService by inject()

    override fun assignKitToPlayer(player: OfflinePlayer) {
        kitService.assignKit(player)
    }

    override fun removeKitFromPlayer(player: OfflinePlayer) {
        kitService.unassignKit(player)
    }
}

/** Allows players to switch kits, but only after they respawn from death */
class KitHandlerWithSwitchingAfterDeath(private val minigame: BrawlMinigame) :
    IKitHandler, KoinComponent {
    private val kitService: KitService by inject()

    override fun assignKitToPlayer(player: OfflinePlayer) {
        kitService.assignKit(player)
    }

    override fun removeKitFromPlayer(player: OfflinePlayer) {
        kitService.unassignKit(player)
    }
}

class BrawlMinigame(val minigameDef: MinigameDef, val teams: List<MinigameTeam>) :
    Manageable(), KoinComponent {

    private var state: MinigameState = MinigameState.PREFLIGHT
    private var gameStartTime: kotlin.time.TimeSource.Monotonic.ValueTimeMark? = null


    // Managers (composable) - select based on minigame type
    val teleportationManager: ITeleportationManager = DefaultTeleportationManager(this)
    val worldManager: IWorldManager = DefaultWorldManager(this)
    val teamManager: ITeamManager = DefaultTeamManager(this)
    val respawnManager: IRespawnManager = DefaultRespawnManager(this)
    val combatManager: ICombatManager = DefaultCombatManager(this)
    val hazardManager: IHazardManager = DefaultHazardManager(this)
    val countdownManager: ICountdownManager = DefaultCountdownManager(this)
    val scoreboardManager: IScoreboardManager = DefaultScoreboardManager()
    val connectionManager: IPlayerConnectionManager = DefaultPlayerConnectionManager(this)
    val environmentProtectionManager: IEnvironmentProtectionManager =
        DefaultEnvironmentProtectionManager(this)

    // Game objective manager - selected based on minigame definition
    val objectiveManager: IGameObjectiveManager =
        when (minigameDef) {
            is FfaMinigameDef -> FfaGameObjectiveManager(this)
            is TeamBasedStocksMinigameDef -> TeamBasedStocksObjectiveManager(this)
        }

    // Kit handler is pluggable
    val kitHandler: IKitHandler =
        when (minigameDef.kitSwitchingMode) {
            KitSwitchingMode.NEVER -> DefaultKitHandler(this)
            KitSwitchingMode.ON_DEATH -> KitHandlerWithSwitchingAfterDeath(this)
            KitSwitchingMode.IMMEDIATE -> KitHandlerWithSwitching(this)
        }

    fun allPlayers(): List<OfflinePlayer> = teams.flatMap { it.players }

    fun getState(): MinigameState = state

    fun getKitSwitchingMode(): KitSwitchingMode = minigameDef.kitSwitchingMode

    fun isPassiveValid(id: String): Boolean = minigameDef.isPassiveValid(id)

    suspend fun setup(gameId: String) {
        // Load world first
        worldManager.loadWorld(gameId)

        // Initialize all managers
        teamManager.setup()
        hazardManager.setup()
        combatManager.setup()
        respawnManager.setup()
        connectionManager.setup()
        objectiveManager.setup()
        environmentProtectionManager.setup()
        scoreboardManager.setup()

        // Setup kit switching event handling
        setupKitSwitchingEvents()

        // Fire join analytics events
        allPlayers().forEach { player ->
            if (player.isOnline) {
                PlayerJoinMinigameAnalyticsEvent(player.player!!, this).callEvent()
            }
        }

        // Teleport teams to starting locations
        teams.forEach { team ->
            teleportationManager.teleportTeamToStartingLocation(team)
            // Assign kits to all players in the team
            team.players.forEach { player ->
                if (player.isOnline) {
                    kitHandler.assignKitToPlayer(player)
                }
            }
        }

        state = MinigameState.STARTING
    }

    private fun setupKitSwitchingEvents() {
        // Handle kit selection events based on switching mode
        listeners.add(
            event<PlayerSelectKitEvent> {
                // Only handle if this player is in this minigame
                if (!allPlayers().any { it.isOnline && it.player?.uniqueId == player.uniqueId })
                    return@event

                when (minigameDef.kitSwitchingMode) {
                    KitSwitchingMode.IMMEDIATE -> {
                        if (shouldSwitchImmediately && !respawnManager.isRespawning(player)) {
                            // Switch kit immediately (but not if player is currently respawning)
                            kitHandler.removeKitFromPlayer(player)
                            kitHandler.assignKitToPlayer(player)
                        }
                    }
                    else -> {}
                }
            }
        )
    }

    fun startGame() {
        if (state != MinigameState.STARTING) return
        state = MinigameState.IN_PROGRESS
        gameStartTime = kotlin.time.TimeSource.Monotonic.markNow()
    }

    fun endMinigame(reason: String, winners: List<OfflinePlayer> = emptyList()) {
        if (state == MinigameState.ENDED) return

        state = MinigameState.ENDING

        val gameDuration = gameStartTime?.elapsedNow() ?: kotlin.time.Duration.ZERO

        // Fire analytics event
        MinigameEndAnalyticsEvent(
                this,
                winners.mapNotNull { if (it.isOnline) it.player else null },
                gameDuration,
                reason,
            )
            .callEvent()

        // TODO: Show victory screen, handle rewards, etc.

        state = MinigameState.ENDED

        allPlayers().forEach { player ->
            if (player.isOnline) {
                kitHandler.removeKitFromPlayer(player)
            }
        }

        teardown()
    }

    override fun teardown() {
        super.teardown()
        (hazardManager as? Manageable)?.teardown()
        (combatManager as? Manageable)?.teardown()
        (scoreboardManager as? Manageable)?.teardown()
        (respawnManager as? Manageable)?.teardown()
        (connectionManager as? Manageable)?.teardown()
        (objectiveManager as? Manageable)?.teardown()
        (environmentProtectionManager as? Manageable)?.teardown()
        (worldManager as? Manageable)?.teardown()
        (countdownManager as? Manageable)?.teardown()
        (teamManager as? Manageable)?.teardown()
    }
}
