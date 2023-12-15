package dev.betrix.supersmashmobsbrawl.listeners

import dev.betrix.supersmashmobsbrawl.SSMBPlayer
import dev.betrix.supersmashmobsbrawl.abilities.tryDoubleJump
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerToggleFlightEvent

class PlayerToggleFlightListener : Listener {

    @EventHandler
    fun onPlayerToggleFlight(event: PlayerToggleFlightEvent) {
        val player = SSMBPlayer.fromUuid(event.player.uniqueId) ?: return

        if (player.bukkitPlayer.gameMode == GameMode.CREATIVE) {
            return
        }

        event.isCancelled = true

        val hasDoubleJumpPassive = player.passives.find {
            it.id == "double_jump"
        } !== null

        if (hasDoubleJumpPassive) {
            tryDoubleJump(player)
        }
    }
}