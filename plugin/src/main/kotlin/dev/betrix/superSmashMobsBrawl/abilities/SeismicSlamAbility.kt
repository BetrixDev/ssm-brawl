package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.events.BrawlDamageEvent
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.extensions.doKnockback
import dev.betrix.superSmashMobsBrawl.extensions.playSound
import gg.flyte.twilight.event.event
import gg.flyte.twilight.extension.getNearbyEntities
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.util.Vector

class SeismicSlamAbility(player: Player) : BrawlAbility("seismic_slam", player) {

    private var isSlamming = false
    private val launchVelocity = metadata.double("launchVelocity") ?: 1.2
    private val forwardVelocity = metadata.double("forwardVelocity") ?: 0.8
    private val slamRadius = metadata.double("slamRadius") ?: 4.0
    private val slamDamage = metadata.double("slamDamage") ?: 8.0
    private val slamKnockbackMultiplier = metadata.double("slamKnockbackMultiplier") ?: 2.0

    override fun setup() {
        listeners.add(
            event<PlayerMoveEvent> {
                if (this@SeismicSlamAbility.player != player || !isSlamming) {
                    return@event
                }

                // Check if player has landed (velocity is low and not on ground)
                if (player.velocity.y < -0.5 && !player.isOnGround) {
                    return@event
                }

                // Player has landed, trigger slam effect
                if (player.isOnGround && player.velocity.y <= 0) {
                    performSeismicSlam()
                    isSlamming = false
                }
            }
        )

        super.setup()
    }

    override fun activate() {
        super.activate()

        // Launch player into air and slightly forward
        val direction = player.location.direction.normalize()
        val launchVector = Vector(
            direction.x * forwardVelocity,
            launchVelocity,
            direction.z * forwardVelocity
        )

        player.velocity = launchVector
        player.playSound(Sound.ENTITY_IRON_GOLEM_HURT, volume = 1.5f, pitch = 0.8f)
        isSlamming = true

        // Prevent fall damage during the slam
        // This will be handled by the plugin's fall damage prevention system
    }

    override fun teardown() {
        isSlamming = false
        super.teardown()
    }

    private fun performSeismicSlam() {
        val location = player.location

        // Play slam sound and particles
        player.world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.6f)
        player.world.spawnParticle(Particle.EXPLOSION, location, 10, 1.0, 0.0, 1.0, 0.5)

        // Damage nearby entities
        damageNearbyEntities(location)

        // Launch player slightly up to simulate impact
        player.velocity = Vector(0.0, 0.5, 0.0)
    }


    private fun damageNearbyEntities(location: org.bukkit.Location) {
        player.getNearbyEntities(slamRadius)
            .filterIsInstance<LivingEntity>()
            .filter { it != player }
            .forEach { entity ->
                val distance = location.distance(entity.location)
                val damageMultiplier = 1.0 - (distance / slamRadius) * 0.5
                val actualDamage = slamDamage * damageMultiplier.coerceAtLeast(0.3)

                val damageEvent = BrawlDamageEvent(
                    entity,
                    Damager.DamagerLivingEntity(player),
                    actualDamage,
                    slamKnockbackMultiplier,
                    dev.betrix.superSmashMobsBrawl.events.BrawlDamageType.MeleeAttack
                )

                damageEvent.callEvent()

                // Apply knockback
                entity.doKnockback(
                    slamKnockbackMultiplier,
                    actualDamage,
                    entity.health,
                    location.toVector(),
                    null
                )
            }
    }

}
