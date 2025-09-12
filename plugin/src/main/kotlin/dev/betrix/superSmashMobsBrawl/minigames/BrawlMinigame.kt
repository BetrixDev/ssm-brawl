package dev.betrix.superSmashMobsBrawl.minigames

import dev.betrix.superSmashMobsBrawl.models.brawlData.KitDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import dev.betrix.superSmashMobsBrawl.services.KitService
import org.bukkit.OfflinePlayer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID


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

class BrawlMinigame(val minigameDef: MinigameDef, val teams: List<MinigameTeam>) {

    private var state: MinigameState = MinigameState.PREFLIGHT

    private val teleportationHandler: ITeleportationHandler = DefaultTeleportationHandler(this)

    init {}
}