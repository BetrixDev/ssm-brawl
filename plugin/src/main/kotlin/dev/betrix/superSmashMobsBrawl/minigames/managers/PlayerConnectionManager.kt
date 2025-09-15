package dev.betrix.superSmashMobsBrawl.minigames.managers

import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.events.PlayerLeaveMinigameAnalyticsEvent
import dev.betrix.superSmashMobsBrawl.minigames.BrawlMinigame
import dev.betrix.superSmashMobsBrawl.minigames.IKitHandler
import dev.betrix.superSmashMobsBrawl.minigames.ITeleportationHandler
import dev.betrix.superSmashMobsBrawl.minigames.MinigameState
import gg.flyte.twilight.event.event
import org.bukkit.GameMode
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

interface IPlayerConnectionManager {
    fun initialize(minigame: BrawlMinigame)
    
    fun markDisconnected(player: OfflinePlayer)

    fun markReconnected(player: OfflinePlayer)

    fun isDisconnected(player: OfflinePlayer): Boolean
    
    fun handlePlayerLeave(player: Player)
    
    fun handlePlayerDisconnect(player: Player)
    
    fun handlePlayerReconnect(player: Player): Boolean
    
    fun canPlayerLeaveMinigame(player: Player): Boolean
}

class DefaultPlayerConnectionManager : Manageable(), IPlayerConnectionManager {
    private val disconnected = mutableSetOf<java.util.UUID>()
    private lateinit var minigame: BrawlMinigame

    override fun initialize(minigame: BrawlMinigame) {
        this.minigame = minigame
        
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
                handlePlayerReconnect(player)
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
    
    override fun handlePlayerLeave(player: Player) {
        // Fire analytics event
        PlayerLeaveMinigameAnalyticsEvent(player, minigame, "manual").callEvent()
        
        // Remove kit
        (minigame.kitHandler as? IKitHandler)?.removeKitFromPlayer(player)
        
        // Mark as disconnected
        if (!isDisconnected(player)) {
            markDisconnected(player)
        }
        
        // Clean up respawning state if player leaves while respawning
        minigame.respawnManager.clearRespawning(player)
        
        // Check if minigame should end
        checkAndHandleMinigameEnd()
    }
    
    override fun handlePlayerDisconnect(player: Player) {
        if (!isPlayerInMinigame(player)) return

        markDisconnected(player)
        handlePlayerLeave(player)
        checkAndHandleMinigameEnd()
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
            (minigame.teleportationHandler as? ITeleportationHandler)?.teleportPlayerRandomSpawnPoint(player)
            (minigame.kitHandler as? IKitHandler)?.assignKitToPlayer(player)
            player.gameMode = GameMode.SURVIVAL
        }

        return true
    }
    
    private fun isPlayerInMinigame(player: Player): Boolean {
        return minigame.allPlayers().any { 
            it.isOnline && it.player?.uniqueId == player.uniqueId 
        }
    }
    
    /** Checks if the minigame should end due to insufficient players and takes appropriate action */
    private fun checkAndHandleMinigameEnd() {
        if (minigame.getState() == MinigameState.ENDED) return

        // Count active players (not spectator mode, still connected, and not disconnected)
        val activePlayers = minigame.allPlayers().filter { player ->
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
    }
}
