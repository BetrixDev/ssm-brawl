package dev.betrix.superSmashMobsBrawl.abilities.instances

import dev.betrix.superSmashMobsBrawl.abilities.definitions.AbilityDefinition
import dev.betrix.superSmashMobsBrawl.extensions.doKnockback
import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.delay
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class ExplosionAbilityInstance(
    definition: AbilityDefinition,
    player: Player
) : AbilityInstance(definition, player) {
    
    private val explosionDamage = 12.0
    private val knockbackMultiplier = 3.0
    private val explosionRadius = 6.0
    private val chargeTime = 40 // 2 seconds (40 ticks)
    private var isCharging = false
    private var chargeTask: gg.flyte.twilight.scheduler.TwilightRunnable? = null

    override fun canActivate(): Boolean {
        return super.canActivate() && !isCharging
    }

    override fun setup() {
        // No manual event handling needed - managed by HotbarService
    }

    override fun teardown() {
        chargeTask?.cancel()
        if (isCharging) {
            stopCharging()
        }
    }

    override fun activate(): Boolean {
        if (!canActivate()) {
            if (isOnCooldown()) {
                player.sendMessage("§cExplosion is on cooldown! (${getRemainingCooldown()}s)")
            } else if (isCharging) {
                player.sendMessage("§cAlready charging explosion!")
            }
            return false
        }

        setCooldown()
        startCharging()
        return true
    }

    private fun startCharging() {
        isCharging = true
        player.sendMessage("§cCharging explosion... §7(Hold still for 2 seconds)")
        
        // Visual and audio feedback during charging
        var ticksRemaining = chargeTime
        chargeTask = repeatingTask(1) {
            if (!isCharging) {
                this.cancel()
                return@repeatingTask
            }
            
            ticksRemaining--
            
            // Particle effects during charging
            player.world.spawnParticle(
                Particle.SMOKE, 
                player.location.add(0.0, 1.0, 0.0), 
                3, 
                0.3, 0.3, 0.3, 0.02
            )
            
            // Increasing intensity as explosion approaches
            if (ticksRemaining % 5 == 0) {
                val pitch = 1.0f + (chargeTime - ticksRemaining) * 0.05f
                player.world.playSound(player.location, Sound.ENTITY_CREEPER_PRIMED, 0.8f, pitch)
            }
            
            // Final countdown particles
            if (ticksRemaining <= 10) {
                player.world.spawnParticle(
                    Particle.ELECTRIC_SPARK,
                    player.location.add(0.0, 1.0, 0.0),
                    5,
                    0.5, 0.5, 0.5, 0.1
                )
            }
            
            if (ticksRemaining <= 0) {
                explode()
                this.cancel()
            }
        }
        
        // Cancel if player moves significantly
        val startLocation = player.location.clone()
        delay(1) {
            checkMovement(startLocation)
        }
    }
    
    private fun checkMovement(startLocation: org.bukkit.Location) {
        if (!isCharging) return
        
        repeatingTask(1) {
            if (!isCharging) {
                this.cancel()
                return@repeatingTask
            }
            
            val currentLocation = player.location
            val distance = startLocation.distance(currentLocation)
            
            // If player moved more than 1.5 blocks, cancel explosion
            if (distance > 1.5) {
                player.sendMessage("§cExplosion cancelled! You moved too far!")
                stopCharging()
                this.cancel()
            }
        }
    }

    private fun stopCharging() {
        isCharging = false
        chargeTask?.cancel()
        chargeTask = null
    }

    private fun explode() {
        if (!isCharging) return
        
        val explosionLocation = player.location.clone()
        isCharging = false
        chargeTask = null
        
        // Visual explosion effects
        explosionLocation.world?.spawnParticle(Particle.EXPLOSION, explosionLocation, 3, 0.1, 0.1, 0.1, 0.0)
        explosionLocation.world?.spawnParticle(Particle.EXPLOSION_EMITTER, explosionLocation, 1)
        explosionLocation.world?.spawnParticle(Particle.SMOKE, explosionLocation, 20, 2.0, 1.0, 2.0, 0.1)
        explosionLocation.world?.spawnParticle(Particle.FLAME, explosionLocation, 15, 1.5, 1.0, 1.5, 0.1)
        
        // Sound effects
        explosionLocation.world?.playSound(explosionLocation, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.8f)
        explosionLocation.world?.playSound(explosionLocation, Sound.ENTITY_CREEPER_DEATH, 1.0f, 1.2f)
        
        // Damage nearby enemies
        damageNearbyEnemies(explosionLocation)
        
        // Apply temporary invulnerability and spectator mode to simulate "death"
        player.gameMode = GameMode.SPECTATOR
        player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 60, 4, false, false))
        
        // Respawn player after brief delay
        delay(60) { // 3 seconds
            respawnPlayer(explosionLocation)
        }
        
        player.sendMessage("§cEXPLOSION! §eYou sacrificed yourself!")
    }

    private fun damageNearbyEnemies(location: org.bukkit.Location) {
        val nearbyPlayers = location.world?.getNearbyEntities(
            location,
            explosionRadius,
            explosionRadius,
            explosionRadius
        )?.filterIsInstance<Player>()?.filter { it != player && it.gameMode != GameMode.SPECTATOR }

        nearbyPlayers?.forEach { target ->
            val distance = target.location.distance(location)
            if (distance <= explosionRadius) {
                val damageMultiplier = 1.0 - (distance / explosionRadius) * 0.4
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
                
                // Screen shake effect (blindness for short duration)
                target.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 10, 0, false, false))
                
                player.sendMessage("§6Explosion hit ${target.name} for §c${finalDamage.toInt()} §6damage!")
            }
        }
    }
    
    private fun respawnPlayer(explosionLocation: org.bukkit.Location) {
        player.gameMode = GameMode.SURVIVAL
        
        // Find a safe respawn location nearby
        val respawnLocation = findSafeRespawnLocation(explosionLocation) ?: explosionLocation.add(0.0, 5.0, 0.0)
        
        player.teleport(respawnLocation)
        
        // Restore health
        player.health = player.maxHealth
        player.foodLevel = 20
        player.saturation = 20.0f
        
        // Brief invulnerability
        player.addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, 40, 4, false, false))
        
        // Respawn effects
        respawnLocation.world?.spawnParticle(Particle.TOTEM_OF_UNDYING, respawnLocation, 20, 1.0, 1.0, 1.0, 0.1)
        respawnLocation.world?.playSound(respawnLocation, Sound.ITEM_TOTEM_USE, 1.0f, 1.2f)
        
        player.sendMessage("§aYou have respawned!")
    }
    
    private fun findSafeRespawnLocation(center: org.bukkit.Location): org.bukkit.Location? {
        val world = center.world ?: return null
        
        // Try to find a safe location within 10 blocks
        for (attempts in 0..20) {
            val offsetX = (Math.random() - 0.5) * 20
            val offsetZ = (Math.random() - 0.5) * 20
            val testLocation = center.clone().add(offsetX, 0.0, offsetZ)
            
            // Find highest solid block
            for (y in world.maxHeight downTo world.minHeight) {
                val block = world.getBlockAt(testLocation.blockX, y, testLocation.blockZ)
                if (block.type.isSolid) {
                    val safeLocation = org.bukkit.Location(world, testLocation.x, y + 1.1, testLocation.z)
                    if (safeLocation.block.type.isAir && safeLocation.add(0.0, 1.0, 0.0).block.type.isAir) {
                        return safeLocation
                    }
                    break
                }
            }
        }
        
        return null
    }
}