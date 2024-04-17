package net.ssmb.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent

class PlayerDropItemListener : Listener {
    @EventHandler
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        if (event.player.gameMode != org.bukkit.GameMode.CREATIVE) {
            event.isCancelled = true
        }
    }
}
