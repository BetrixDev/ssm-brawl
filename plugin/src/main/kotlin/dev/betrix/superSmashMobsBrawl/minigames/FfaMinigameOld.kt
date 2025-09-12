package dev.betrix.superSmashMobsBrawl.minigames

import dev.betrix.superSmashMobsBrawl.models.MinigamePlayer
import dev.betrix.superSmashMobsBrawl.models.MinigameState
import dev.betrix.superSmashMobsBrawl.models.brawlData.FfaMinigameDef
import org.bukkit.GameMode
import org.bukkit.entity.Player

open class FfaMinigameOld(minigameId: String, gameId: String, players: List<MinigamePlayer>) :
    BrawlMinigameOld<FfaMinigameDef>(minigameId, gameId, players) {

    override fun canPlayerLeaveMinigame(player: Player): Boolean {
        return true
    }

    override fun onPlayerLeave(player: Player) {
        // Call parent implementation to handle kit cleanup
        super.onPlayerLeave(player)

        // Check if minigame should end due to insufficient players
        checkAndHandleMinigameEnd()
    }

    override fun checkAndHandleMinigameEnd() {
        if (state == MinigameState.ENDED) return

        // Count active players (not spectator mode and still connected)
        val activePlayers =
            players.filter { player ->
                val bukkitPlayer = player.player
                bukkitPlayer.isOnline &&
                    !disconnectedPlayers.contains(bukkitPlayer.uniqueId) &&
                    bukkitPlayer.gameMode != GameMode.SPECTATOR
            }

        // For FFA, end game if 1 or fewer players remain
        if (activePlayers.size <= 1) {
            endFfaMinigame(activePlayers.firstOrNull())
        }
    }

    private fun endFfaMinigame(winner: MinigamePlayer?) {
        if (state == MinigameState.ENDED) return
        state = MinigameState.ENDED

        // Handle winner announcement and cleanup
        // This would typically be handled by a more general minigame ending system
        // For now, just mark as ended
        teardown()
    }
}
