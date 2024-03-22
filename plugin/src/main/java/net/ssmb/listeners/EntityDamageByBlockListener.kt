package net.ssmb.listeners

import net.ssmb.SSMB
import net.ssmb.extensions.getMetadata
import net.ssmb.utils.TaggedKeyBool
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByBlockEvent
import org.bukkit.event.entity.EntityDamageEvent

class EntityDamageByBlockListener : Listener {
    val plugin = SSMB.instance

    @EventHandler
    fun onEntityDamageByBlock(event: EntityDamageByBlockEvent) {
        if (event.cause == EntityDamageEvent.DamageCause.FALL) {
            val canTakeFallDamage = event.entity.getMetadata(TaggedKeyBool("can_take_fall_damage"))

            if (canTakeFallDamage != true) {
                event.isCancelled = true
            }
        } else if (event.cause == EntityDamageEvent.DamageCause.SUFFOCATION && event.entity is Player) {
            event.isCancelled = true
            event.entity.teleport(plugin.hub.spawnLocation)
        }
    }
}