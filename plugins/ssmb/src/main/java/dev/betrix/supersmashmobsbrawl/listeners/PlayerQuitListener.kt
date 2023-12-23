package dev.betrix.supersmashmobsbrawl.listeners

import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener : Listener {
    private val plugin = SuperSmashMobsBrawl.instance

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        if (plugin.editorManager.isPlayerInSession(event.player)) {
            plugin.editorManager.removePlayerFromSession(event.player)
        }
    }
}