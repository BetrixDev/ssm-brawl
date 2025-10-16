package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.events.BrawlDamageEvent
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageType
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.extensions.setVelocity
import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player

class SuperSquidAbility(player: Player) : BrawlAbility("super_squid", player) {

    private var activeTask: TwilightRunnable? = null
    private var isActive: Boolean = false

    override fun activate() {
        super.activate()

        if (activeTask != null) {
            return
        }

        var ticks = 0
        val maxTicks = 22

        activeTask =
            repeatingTask(0, 0) {
                // Check if the kit is still valid
                val kit = kitService.getKitForPlayer(player)
                if (kit == null) {
                    stopAbility()
                    cancel()
                    return@repeatingTask
                }

                // Check if player is still blocking or if max duration reached
                if (!player.isBlocking || ticks >= maxTicks) {
                    stopAbility()
                    cancel()
                    return@repeatingTask
                }

                // Mark as active and set invincibility
                isActive = true
                kit.setInvincible(true)

                // Apply velocity in the direction the player is looking
                player.setVelocity(0.6, 0.1, 1.0, true)

                // Play splash sound
                player.world.playSound(player.location, Sound.ENTITY_GENERIC_SPLASH, 0.5f, 1.0f)

                // Spawn water splash particles
                player.world.spawnParticle(
                    Particle.DRIPPING_WATER,
                    player.location.clone().add(0.0, 0.5, 0.0),
                    60,
                    0.3,
                    0.3,
                    0.3,
                    0.0,
                )

                ticks++
            }

        activeTask?.let { runnables.add(it) }
    }

    private fun stopAbility() {
        isActive = false
        val kit = kitService.getKitForPlayer(player)
        kit?.setInvincible(false)
        activeTask = null
    }

    override fun teardown() {
        activeTask?.cancel()
        stopAbility()
        super.teardown()
    }

    override fun setup() {
        super.setup()

        // Cancel owner's melee attacks while ability is active
        listeners.add(
            event<BrawlDamageEvent> {
                if (!isActive) {
                    return@event
                }

                val damagerEntity = (damager as? Damager.DamagerLivingEntity)?.livingEntity
                if (damagerEntity != this@SuperSquidAbility.player) {
                    return@event
                }

                // Only cancel melee attacks, not other damage types
                if (damageType != BrawlDamageType.MeleeAttack) {
                    return@event
                }

                isCancelled = true
            }
        )
    }
}
