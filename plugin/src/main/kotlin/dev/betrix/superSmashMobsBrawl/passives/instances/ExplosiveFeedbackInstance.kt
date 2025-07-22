package dev.betrix.superSmashMobsBrawl.passives.instances

import dev.betrix.superSmashMobsBrawl.extensions.doKnockback
import dev.betrix.superSmashMobsBrawl.passives.definitions.PassiveDefinition
import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.delay
import org.bukkit.GameMode
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent

class ExplosiveFeedbackInstance(
    definition: PassiveDefinition,
    player: Player
) : PassiveInstance(definition, player) {
    
    private val explosionDamage = 3.0
    private val knockbackMultiplier = 1.2
    private val explosionRadius = 3.5
    private var lastExplosionTime = 0L
    private val explosionCooldown = 1000 // 1 second cooldown between explosions

    override fun setup() {
        event<EntityDamageByEntityEvent> {
            if (entity != this@ExplosiveFeedbackInstance.player) return@event
            if (damage <= 0) return@event
            
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastExplosionTime < explosionCooldown) return@event
            
            lastExplosionTime = currentTime
            
            // Slight delay to ensure damage is processed first
            delay(2) {
                createExplosiveFeedback()
            }
        }
    }

    override fun teardown() {
        // Event listeners are automatically cleaned up by Twilight
    }
    
    private fun createExplosiveFeedback() {
        val explosionLocation = player.location.clone()
        
        // Visual explosion effects (smaller than main explosion)
        explosionLocation.world?.spawnParticle(Particle.EXPLOSION, explosionLocation, 1, 0.1, 0.1, 0.1, 0.0)
        explosionLocation.world?.spawnParticle(Particle.SMOKE, explosionLocation, 8, 1.0, 0.5, 1.0, 0.05)
        explosionLocation.world?.spawnParticle(Particle.FLAME, explosionLocation, 5, 0.8, 0.5, 0.8, 0.05)
        
        // Sound effects
        explosionLocation.world?.playSound(explosionLocation, Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 1.5f)
        explosionLocation.world?.playSound(explosionLocation, Sound.ENTITY_CREEPER_HURT, 0.8f, 1.0f)
        
        // Damage nearby enemies
        damageNearbyEnemies(explosionLocation)
    }
    
    private fun damageNearbyEnemies(location: org.bukkit.Location) {
        val nearbyPlayers = location.world?.getNearbyEntities(
            location,
            explosionRadius,
            explosionRadius,
            explosionRadius
        )?.filterIsInstance<Player>()?.filter { 
            it != player && it.gameMode != GameMode.SPECTATOR 
        }

        if (nearbyPlayers?.isEmpty() != false) {
            // No enemies hit, just show self-defense effect
            player.sendMessage("§7Explosive feedback activated!")
            return
        }

        nearbyPlayers.forEach { target ->
            val distance = target.location.distance(location)
            if (distance <= explosionRadius) {
                val damageMultiplier = 1.0 - (distance / explosionRadius) * 0.5
                val finalDamage = explosionDamage * damageMultiplier.coerceAtLeast(0.3)
                val finalKnockback = knockbackMultiplier * damageMultiplier

                target.doKnockback(
                    finalKnockback,
                    finalDamage,
                    target.health,
                    location.toVector(),
                    null
                )
                target.damage(finalDamage, player)
                
                // Additional particle effect on hit target
                target.world.spawnParticle(Particle.CRIT, target.location.add(0.0, 1.0, 0.0), 5, 0.3, 0.5, 0.3, 0.1)
                
                player.sendMessage("§6Explosive feedback hit ${target.name} for §c${finalDamage.toInt()} §6damage!")
            }
        }
    }
}