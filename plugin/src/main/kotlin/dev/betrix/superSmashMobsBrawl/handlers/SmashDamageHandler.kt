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

class SmashDamageHandler(private val plugin: Plugin) {
    
    init {
        registerEventListeners()
    }
    
    private fun registerEventListeners() {
        event<EntityDamageByEntityEvent> {
            if (entity !is Player) return@event
            if (!SmashDamageService.isPlayerInMinigame(entity as Player)) return@event
            
            val victim = entity as Player
            val damager = damager
            
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
            
            val knockback = run {
                val direction = victim.location.toVector().subtract(damager.location.toVector()).normalize()
                direction.multiply(0.5)
            }
            
            val extraData = mutableMapOf<String, Any>()
            if (damager is org.bukkit.entity.Projectile) {
                extraData["projectile_type"] = damager.type.name
            }
            
            val smashEvent = SmashDamageService.fireDamageEvent(
                victim = victim,
                damager = damager,
                damage = damage,
                knockback = knockback,
                damageSource = damageSource,
                extraData = extraData
            )
            
            if (smashEvent.isCancelled) {
                isCancelled = true
                return@event
            }
            
            if (smashEvent.damage != damage) {
                damage = smashEvent.damage
            }
        }
        
        event<EntityDamageEvent> {
            if (entity !is Player) return@event
            if (!SmashDamageService.isPlayerInMinigame(entity as Player)) return@event
            
            val victim = entity as Player
            
            val damageSource = when (cause.name) {
                "FALL" -> SmashDamageEvent.DamageSource.FALL
                "EXPLOSION" -> SmashDamageEvent.DamageSource.EXPLOSION
                "LAVA", "FIRE", "FIRE_TICK", "DROWNING", "SUFFOCATION", "CONTACT", "CRAMMING" -> SmashDamageEvent.DamageSource.ENVIRONMENT
                else -> SmashDamageEvent.DamageSource.OTHER
            }
            
            val extraData = mutableMapOf<String, Any>()
            if (cause.name == "FALL") {
                extraData["fall_distance"] = victim.fallDistance
            }
            
            val smashEvent = SmashDamageService.fireDamageEvent(
                victim = victim,
                damage = damage,
                damageSource = damageSource,
                extraData = extraData
            )
            
            if (smashEvent.isCancelled) {
                isCancelled = true
                return@event
            }
            
            if (smashEvent.damage != damage) {
                damage = smashEvent.damage
            }
        }
    }
}