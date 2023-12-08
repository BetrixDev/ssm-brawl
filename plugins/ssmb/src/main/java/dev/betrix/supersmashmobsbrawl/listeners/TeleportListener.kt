package dev.betrix.supersmashmobsbrawl.listeners

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent

class TeleportListener : Listener {
    @EventHandler
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        Bukkit.getServer().logger.info("Teleporting player between worlds")
    }
}