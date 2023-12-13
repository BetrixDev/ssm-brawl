package dev.betrix.supersmashmobsbrawl.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent

class PlayerDropItemListener : Listener {

    @EventHandler
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        event.isCancelled = true
    }
}