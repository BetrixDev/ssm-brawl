package dev.betrix.supersmashmobsbrawl.listeners

import io.papermc.paper.event.player.PlayerPickItemEvent
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PlayerPickItemListener : Listener {

    @EventHandler
    fun onPlayerPickItem(event: PlayerPickItemEvent) {
        if (event.player.gameMode != GameMode.CREATIVE) {
            event.isCancelled = true
        }
    }
}