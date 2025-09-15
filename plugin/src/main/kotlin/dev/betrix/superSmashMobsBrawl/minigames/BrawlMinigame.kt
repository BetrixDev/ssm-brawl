package dev.betrix.superSmashMobsBrawl.minigames

import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.models.brawlData.KitDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.KitSwitchingMode
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import dev.betrix.superSmashMobsBrawl.services.KitService
import java.util.UUID
import org.bukkit.OfflinePlayer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import dev.betrix.superSmashMobsBrawl.minigames.managers.DefaultWorldManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.IWorldManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.DefaultTeamManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.ITeamManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.DefaultRespawnManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.IRespawnManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.DefaultCombatManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.ICombatManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.DefaultHazardManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.IHazardManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.DefaultCountdownManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.ICountdownManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.DefaultScoreboardManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.IScoreboardManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.DefaultPlayerConnectionManager
import dev.betrix.superSmashMobsBrawl.minigames.managers.IPlayerConnectionManager


data class MinigameTeam(val players: List<OfflinePlayer>, val id: String = UUID.randomUUID().toString(), val name: String) {
    init {
        require(players.isNotEmpty()) { "A team must have at least one player." }
    }
}

enum class MinigameState {
    PREFLIGHT,
    STARTING,
    IN_PROGRESS,
    ENDING,
    ENDED
}

interface ITeleportationHandler {
    fun teleportTeamToStartingLocation(team: MinigameTeam)
    fun teleportPlayerToSpectatorArea(player: OfflinePlayer)
    fun teleportPlayerRandomSpawnPoint(player: OfflinePlayer)
}

class DefaultTeleportationHandler(private val minigame: BrawlMinigame) : ITeleportationHandler {
    override fun teleportTeamToStartingLocation(team: MinigameTeam) {
        // Default implementation does nothing
    }

    override fun teleportPlayerToSpectatorArea(player: OfflinePlayer) {
        // Default implementation does nothing
    }

    override fun teleportPlayerRandomSpawnPoint(player: OfflinePlayer) {
        // Default implementation does nothing
    }
}

interface IKitHandler {
    fun assignKitToPlayer(player: OfflinePlayer)
    fun removeKitFromPlayer(player: OfflinePlayer)
}

/**
 * Default kit handler that keeps the player's kit unchanged throughout the game.
 */
class DefaultKitHandler(private val minigame: BrawlMinigame): IKitHandler, KoinComponent {
    private val kitService: KitService by inject()

    private val initialPlayerKits: Map<UUID, KitDef> =
        minigame.teams
            .flatMap { it.players }
            .associate { player -> player.uniqueId to kitService.currentSelectedKitForPlayer(player) }
            .toMap()

    override fun assignKitToPlayer(player: OfflinePlayer) {
        kitService.assignKit(player, initialPlayerKits[player.uniqueId]!!)
    }

    override fun removeKitFromPlayer(player: OfflinePlayer) {
        kitService.unassignKit(player)
    }
}

/**
 * Allows players to switch kits at anytime during the game
 */
class KitHandlerWithSwitching(private val minigame: BrawlMinigame): IKitHandler, KoinComponent {
    private val kitService: KitService by inject()

    override fun assignKitToPlayer(player: OfflinePlayer) {
//        kitService.giveKitWithoutSwitching(player)
    }

    override fun removeKitFromPlayer(player: OfflinePlayer) {
//        kitService.removeKit(player)
    }
}

/**
 * Allows players to switch kits, but only after they respawn from death
 */
class KitHandlerWithSwitchingAfterDeath(private val minigame: BrawlMinigame): IKitHandler, KoinComponent {
    private val kitService: KitService by inject()

    override fun assignKitToPlayer(player: OfflinePlayer) {
        kitService.assignKit(player)
    }

    override fun removeKitFromPlayer(player: OfflinePlayer) {
//        kitService.removeKit(player)
    }
}

interface ICombatHandler {}

class BrawlMinigame(val minigameDef: MinigameDef, val teams: List<MinigameTeam>) : Manageable(), KoinComponent {

    private var state: MinigameState = MinigameState.PREFLIGHT

    private val teleportationHandler: ITeleportationHandler = DefaultTeleportationHandler(this)

    // Managers (composable)
    val worldManager: IWorldManager = DefaultWorldManager()
    val teamManager: ITeamManager = DefaultTeamManager()
    val respawnManager: IRespawnManager = DefaultRespawnManager(this)
    val combatManager: ICombatManager = DefaultCombatManager()
    val hazardManager: IHazardManager = DefaultHazardManager()
    val countdownManager: ICountdownManager = DefaultCountdownManager(this)
    val scoreboardManager: IScoreboardManager = DefaultScoreboardManager()
    val connectionManager: IPlayerConnectionManager = DefaultPlayerConnectionManager()

    // Kit handler is pluggable
    private val kitHandler: IKitHandler = when (minigameDef.kitSwitchingMode) {
        KitSwitchingMode.NEVER -> DefaultKitHandler(this)
        KitSwitchingMode.ON_DEATH -> KitHandlerWithSwitchingAfterDeath(this)
        KitSwitchingMode.IMMEDIATE -> KitHandlerWithSwitching(this)
    }

    fun allPlayers(): List<OfflinePlayer> = teams.flatMap { it.players }

    fun getKitSwitchingMode(): KitSwitchingMode = minigameDef.kitSwitchingMode

    fun isPassiveValid(id: String): Boolean = minigameDef.isPassiveValid(id)

    suspend fun setup(gameId: String) {
        // Load world
        worldManager.loadWorld(minigameDef, gameId)
        // Teams
        teamManager.registerTeams(this)
        // Hazards and combat
        hazardManager.initialize(this)
        combatManager.initialize(this)
        // Scoreboard init
        scoreboardManager.initialize()
        state = MinigameState.STARTING
    }

    override fun teardown() {
        super.teardown()
        hazardManager.teardown()
        combatManager.teardown()
        scoreboardManager.teardown()
        (worldManager as? Manageable)?.teardown()
    }
}