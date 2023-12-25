package dev.betrix.supersmashmobsbrawl.listeners

import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByBlockEvent
import org.bukkit.event.entity.EntityDamageEvent

class EntityDamageByBlockListener : Listener {
    private val plugin = SuperSmashMobsBrawl.instance

    @EventHandler
    fun onEntityDamageByBlock(event: EntityDamageByBlockEvent) {
        if (event.cause == EntityDamageEvent.DamageCause.FALL) {
            event.isCancelled = true
        } else if (event.cause == EntityDamageEvent.DamageCause.SUFFOCATION && event.entity is Player) {
            if (event.entity.world == plugin.hub.worldInstance) {
                plugin.hub.teleportPlayer(event.entity as Player)
            }
        }
    }
}