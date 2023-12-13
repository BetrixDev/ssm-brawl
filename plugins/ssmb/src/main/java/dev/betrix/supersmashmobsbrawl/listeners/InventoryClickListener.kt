package dev.betrix.supersmashmobsbrawl.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class InventoryClickListener : Listener {

    /** Catch-all event that needs to run after our actual inventory interaction events */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInventoryClick(event: InventoryClickEvent) {
        event.isCancelled = true
    }
}