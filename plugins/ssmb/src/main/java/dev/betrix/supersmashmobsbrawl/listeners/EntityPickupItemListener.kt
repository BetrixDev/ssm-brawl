package dev.betrix.supersmashmobsbrawl.listeners

import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent

class EntityPickupItemListener : Listener {

    @EventHandler()
    fun onPlayerPickupItem(event: EntityPickupItemEvent) {
        if (event.entity is Player && (event.entity as Player).gameMode != GameMode.CREATIVE) {
            event.isCancelled = true
        }
    }
}