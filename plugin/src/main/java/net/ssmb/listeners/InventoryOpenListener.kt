package net.ssmb.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType

class InventoryOpenListener : Listener {
    @EventHandler
    fun onInventoryOpen(event: InventoryOpenEvent) {
        if (event.inventory.type != InventoryType.PLAYER) {
            event.isCancelled = true
        }
    }
}
