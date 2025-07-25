package dev.betrix.superSmashMobsBrawl.handlers

import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent
import dev.betrix.superSmashMobsBrawl.services.SmashDamageService
import gg.flyte.twilight.event.event
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.plugin.Plugin
import org.bukkit.util.Vector

/**
 * Handler for converting Bukkit damage events into SmashDamageEvents for mini-game players.
 * This ensures that all damage in mini-games goes through our custom event system.
 */
class SmashDamageHandler(private val plugin: Plugin) {
    
    init {
        registerEventListeners()
    }
    
    private fun registerEventListeners() {
        // Handle entity damage by entity (player attacks, projectiles, etc.)
        event<EntityDamageByEntityEvent> {
            if (entity !is Player) return@event
            if (!SmashDamageService.isPlayerInMinigame(entity as Player)) return@event
            
            val victim = entity as Player
            val damager = damager
            
            // Determine damage source
            val damageSource = when {
                damager is Player -> SmashDamageEvent.DamageSource.MELEE_ATTACK
                damager is org.bukkit.entity.Projectile -> {
                    val shooter = damager.shooter
                    if (shooter is Player) {
                        SmashDamageEvent.DamageSource.PROJECTILE
                    } else {
                        SmashDamageEvent.DamageSource.OTHER
                    }
                }
                else -> SmashDamageEvent.DamageSource.OTHER
            }
            
            // Calculate knockback vector
            val knockback = run {
                val direction = victim.location.toVector().subtract(damager.location.toVector()).normalize()
                direction.multiply(0.5) // Base knockback strength
            }
            
            // Create extra data
            val extraData = mutableMapOf<String, Any>()
            if (damager is org.bukkit.entity.Projectile) {
                extraData["projectile_type"] = damager.type.name
            }
            
            // Fire our custom event
            val smashEvent = SmashDamageService.fireDamageEvent(
                victim = victim,
                damager = damager,
                damage = damage,
                knockback = knockback,
                damageSource = damageSource,
                extraData = extraData
            )
            
            // If our event was cancelled, cancel the original event
            if (smashEvent.isCancelled) {
                isCancelled = true
                return@event
            }
            
            // Apply modified damage if changed
            if (smashEvent.damage != damage) {
                damage = smashEvent.damage
            }
        }
        
        // Handle general entity damage (fall damage, environmental damage, etc.)
        event<EntityDamageEvent> {
            if (entity !is Player) return@event
            if (!SmashDamageService.isPlayerInMinigame(entity as Player)) return@event
            
            val victim = entity as Player
            
            // Determine damage source
            val damageSource = when (cause.name) {
                "FALL" -> SmashDamageEvent.DamageSource.FALL
                "EXPLOSION" -> SmashDamageEvent.DamageSource.EXPLOSION
                "LAVA", "FIRE", "FIRE_TICK", "DROWNING", "SUFFOCATION", "CONTACT", "CRAMMING" -> SmashDamageEvent.DamageSource.ENVIRONMENT
                else -> SmashDamageEvent.DamageSource.OTHER
            }
            
            // Create extra data
            val extraData = mutableMapOf<String, Any>()
            if (cause.name == "FALL") {
                extraData["fall_distance"] = victim.fallDistance
            }
            
            // Fire our custom event
            val smashEvent = SmashDamageService.fireDamageEvent(
                victim = victim,
                damage = damage,
                damageSource = damageSource,
                extraData = extraData
            )
            
            // If our event was cancelled, cancel the original event
            if (smashEvent.isCancelled) {
                isCancelled = true
                return@event
            }
            
            // Apply modified damage if changed
            if (smashEvent.damage != damage) {
                damage = smashEvent.damage
            }
        }
    }
}