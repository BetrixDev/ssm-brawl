package dev.betrix.supersmashmobsbrawl.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByBlockEvent
import org.bukkit.event.entity.EntityDamageEvent

class EntityDamageByBlockListener : Listener {

    @EventHandler
    fun onEntityDamageByBlock(event: EntityDamageByBlockEvent) {
        if (event.cause == EntityDamageEvent.DamageCause.FALL) {
            event.isCancelled = true
        }
    }
}