package net.ssmb.listeners

import io.papermc.paper.event.player.PlayerPickItemEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PlayerPickItemListener : Listener {
    @EventHandler
    fun onPlayerPickItem(event: PlayerPickItemEvent) {
        if (event.player.gameMode != org.bukkit.GameMode.CREATIVE) {
            event.isCancelled = true
        }
    }
}