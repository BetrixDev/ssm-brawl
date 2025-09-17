package dev.betrix.superSmashMobsBrawl.minigames.managers

import dev.betrix.superSmashMobsBrawl.IManageable
import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.events.PlayerLeaveMinigameAnalyticsEvent
import dev.betrix.superSmashMobsBrawl.minigames.BrawlMinigame
import dev.betrix.superSmashMobsBrawl.minigames.IKitHandler
import dev.betrix.superSmashMobsBrawl.minigames.ITeleportationManager
import dev.betrix.superSmashMobsBrawl.minigames.MinigameState
import gg.flyte.twilight.event.event
import org.bukkit.GameMode
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

interface IPlayerConnectionManager : IManageable {
    fun markDisconnected(player: OfflinePlayer)

    fun markReconnected(player: OfflinePlayer)

    fun isDisconnected(player: OfflinePlayer): Boolean

    fun handlePlayerLeave(player: Player, reason: String = "manual")

    fun handlePlayerDisconnect(player: Player)

    fun handlePlayerReconnect(player: Player): Boolean

    fun canPlayerLeaveMinigame(player: Player): Boolean
}

class DefaultPlayerConnectionManager(private val minigame: BrawlMinigame) : Manageable(), IPlayerConnectionManager {
    private val disconnected = mutableSetOf<java.util.UUID>()
    private val hasLeft = mutableSetOf<java.util.UUID>()

    override fun setup() {

        // Listen for player disconnect events
        listeners.add(
            event<PlayerQuitEvent> {
                if (isPlayerInMinigame(player)) {
                    handlePlayerDisconnect(player)
                }
            }
        )

        // Listen for player reconnect events
        listeners.add(
            event<PlayerJoinEvent> {
                if (disconnected.contains(player.uniqueId)) {
                    handlePlayerReconnect(player)
                }
            }
        )
    }

    override fun markDisconnected(player: OfflinePlayer) {
        disconnected.add(player.uniqueId)
    }

    override fun markReconnected(player: OfflinePlayer) {
        disconnected.remove(player.uniqueId)
    }

    override fun isDisconnected(player: OfflinePlayer): Boolean =
        disconnected.contains(player.uniqueId)

    override fun canPlayerLeaveMinigame(player: Player): Boolean {
        return true // Default implementation allows leaving
    }

    override fun handlePlayerLeave(player: Player, reason: String) {
        // Early return if player has already left (idempotent)
        if (hasLeft.contains(player.uniqueId)) {
            return
        }

        // Mark as left immediately to prevent re-entry
        hasLeft.add(player.uniqueId)

        // Fire analytics event with provided reason
        PlayerLeaveMinigameAnalyticsEvent(player, minigame, reason).callEvent()

        // Remove kit
        (minigame.kitHandler as? IKitHandler)?.removeKitFromPlayer(player)

        // Mark as disconnected
        markDisconnected(player)

        // Clean up respawning state if player leaves while respawning
        minigame.respawnManager.clearRespawning(player)

        // Check if minigame should end
        checkAndHandleMinigameEnd()
    }

    override fun handlePlayerDisconnect(player: Player) {
        if (!isPlayerInMinigame(player)) return

        // handlePlayerLeave will handle marking as disconnected and checking minigame end
        handlePlayerLeave(player, "disconnect")
    }

    override fun handlePlayerReconnect(player: Player): Boolean {
        // Only handle if player was previously in this minigame and disconnected
        if (!disconnected.remove(player.uniqueId) || !minigame.minigameDef.allowRejoinAfterLeave) {
            return false
        }

        // Only teleport back if minigame is still active and not ended
        if (minigame.getState() == MinigameState.ENDED) {
            return false
        }

        // Make sure player is still part of this minigame
        if (!isPlayerInMinigame(player)) {
            return false
        }

        // Teleport player back to the minigame world
        val world = minigame.worldManager.getWorld()
        if (world != null) {
            (minigame.teleportationManager as? ITeleportationManager)
                ?.teleportPlayerRandomSpawnPoint(player)
            (minigame.kitHandler as? IKitHandler)?.assignKitToPlayer(player)
            player.gameMode = GameMode.SURVIVAL
        }

        // Clear leave flag and mark reconnected
        hasLeft.remove(player.uniqueId)
        markReconnected(player)

        return true
    }

    private fun isPlayerInMinigame(player: Player): Boolean {
        return minigame.allPlayers().any { it.isOnline && it.player?.uniqueId == player.uniqueId }
    }

    /**
     * Checks if the minigame should end due to insufficient players and takes appropriate action
     */
    private fun checkAndHandleMinigameEnd() {
        if (minigame.getState() == MinigameState.ENDED) return

        // Count active players (not spectator mode, still connected, and not disconnected)
        val activePlayers =
            minigame.allPlayers().filter { player ->
                val bukkitPlayer = player.player
                bukkitPlayer != null &&
                    bukkitPlayer.isOnline &&
                    !isDisconnected(player) &&
                    bukkitPlayer.gameMode != GameMode.SPECTATOR
            }

        // End minigame if no active players remain
        if (activePlayers.isEmpty()) {
            minigame.endMinigame("no_active_players")
        }
    }

    override fun teardown() {
        super.teardown()
        disconnected.clear()
        hasLeft.clear()
    }
}
