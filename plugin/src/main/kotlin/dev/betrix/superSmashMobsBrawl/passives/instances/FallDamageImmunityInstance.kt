package dev.betrix.superSmashMobsBrawl.passives.instances

import dev.betrix.superSmashMobsBrawl.extensions.doKnockback
import dev.betrix.superSmashMobsBrawl.passives.definitions.PassiveDefinition
import gg.flyte.twilight.event.event
import org.bukkit.GameMode
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent

class FallDamageImmunityInstance(
    definition: PassiveDefinition,
    player: Player
) : PassiveInstance(definition, player) {
    
    private val explosionDamage = 4.0
    private val knockbackMultiplier = 1.5
    private val explosionRadius = 4.0
    private val minimumFallDistance = 8.0 // Minimum fall distance to trigger explosion

    override fun setup() {
        event<EntityDamageEvent> {
            if (entity != this@FallDamageImmunityInstance.player) return@event
            if (cause != EntityDamageEvent.DamageCause.FALL) return@event
            
            val fallDistance = this@FallDamageImmunityInstance.player.fallDistance
            
            // Cancel all fall damage
            isCancelled = true
            
            // Create explosion effect if fall was significant
            if (fallDistance >= minimumFallDistance) {
                createLandingExplosion(fallDistance)
            }
            
            // Reset fall distance
            this@FallDamageImmunityInstance.player.fallDistance = 0.0f
        }
    }

    override fun teardown() {
        // Event listeners are automatically cleaned up by Twilight
    }
    
    private fun createLandingExplosion(fallDistance: Float) {
        val explosionLocation = player.location.clone()
        
        // Scale explosion effects based on fall distance
        val explosionScale = (fallDistance / 20.0f).coerceAtMost(2.0f).coerceAtLeast(0.5f)
        
        // Visual explosion effects
        explosionLocation.world?.spawnParticle(
            Particle.EXPLOSION, 
            explosionLocation, 
            (1 * explosionScale).toInt().coerceAtLeast(1), 
            0.1, 0.1, 0.1, 0.0
        )
        explosionLocation.world?.spawnParticle(
            Particle.SMOKE, 
            explosionLocation, 
            (10 * explosionScale).toInt(), 
            1.0 * explosionScale, 0.5, 1.0 * explosionScale, 0.05
        )
        explosionLocation.world?.spawnParticle(
            Particle.BLOCK, 
            explosionLocation, 
            (15 * explosionScale).toInt(), 
            1.5 * explosionScale, 0.1, 1.5 * explosionScale, 0.1, 
            explosionLocation.block.blockData
        )
        
        // Sound effects
        val volume = (0.8f * explosionScale).coerceAtMost(1.5f)
        val pitch = (1.2f - (explosionScale - 1.0f) * 0.3f).coerceIn(0.5f, 2.0f)
        
        explosionLocation.world?.playSound(explosionLocation, Sound.ENTITY_GENERIC_EXPLODE, volume, pitch)
        explosionLocation.world?.playSound(explosionLocation, Sound.BLOCK_STONE_BREAK, volume * 1.2f, 0.8f)
        
        // Damage nearby enemies
        damageNearbyEnemies(explosionLocation, explosionScale)
        
        player.sendMessage("§6Landing explosion! §7(Fall distance: §e${fallDistance.toInt()} blocks§7)")
    }
    
    private fun damageNearbyEnemies(location: org.bukkit.Location, explosionScale: Float) {
        val nearbyPlayers = location.world?.getNearbyEntities(
            location,
            explosionRadius * explosionScale,
            explosionRadius * explosionScale,
            explosionRadius * explosionScale
        )?.filterIsInstance<Player>()?.filter { 
            it != player && it.gameMode != GameMode.SPECTATOR 
        }

        if (nearbyPlayers?.isEmpty() != false) {
            return
        }

        nearbyPlayers.forEach { target ->
            val distance = target.location.distance(location)
            val scaledRadius = explosionRadius * explosionScale
            
            if (distance <= scaledRadius) {
                val damageMultiplier = 1.0 - (distance / scaledRadius) * 0.4
                val finalDamage = explosionDamage * explosionScale * damageMultiplier.coerceAtLeast(0.3)
                val finalKnockback = knockbackMultiplier * explosionScale * damageMultiplier

                target.doKnockback(
                    finalKnockback,
                    finalDamage,
                    target.health,
                    location.toVector(),
                    null
                )
                target.damage(finalDamage, player)
                
                // Additional particle effect on hit target
                target.world.spawnParticle(
                    Particle.CRIT, 
                    target.location.add(0.0, 1.0, 0.0), 
                    (8 * explosionScale).toInt().coerceAtLeast(3), 
                    0.3, 0.5, 0.3, 0.1
                )
                
                player.sendMessage("§6Landing explosion hit ${target.name} for §c${finalDamage.toInt()} §6damage!")
            }
        }
    }
}