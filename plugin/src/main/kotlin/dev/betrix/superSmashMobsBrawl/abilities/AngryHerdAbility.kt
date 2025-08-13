package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent
import dev.betrix.superSmashMobsBrawl.extensions.doKnockback
import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.repeatingTask
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Cow
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDeathEvent

class AngryHerdAbility(player: Player) : BrawlAbility("angry_herd", player) {

    private val cowsCount = metadata.int("cowsCount") ?: 5
    private val cowSpeed = metadata.double("cowSpeed") ?: 0.9
    private val contactRadius = metadata.double("contactRadius") ?: 1.2
    private val durationTicks = metadata.int("durationTicks") ?: 60 // 3s
    private val damagePerHit = metadata.double("damagePerHit") ?: 5.0
    private val knockbackMultiplier = metadata.double("knockbackMultiplier") ?: 1.25
    private val hitCooldownTicks = metadata.int("hitCooldownTicks") ?: 10

    private val activeCowIds = ConcurrentHashMap.newKeySet<UUID>()

    override fun teardown() {
        super.teardown()
    }

    override fun activate() {
        val dir = player.eyeLocation.direction.normalize()

        val spawnedCows = mutableListOf<Cow>()
        repeat(cowsCount) { idx ->
            val spawnLoc = player.location.clone().add(dir.clone().multiply(idx * 0.75))
            val cow = player.world.spawn(spawnLoc, Cow::class.java)
            cow.velocity = dir.clone().multiply(cowSpeed)
            activeCowIds.add(cow.uniqueId)
            spawnedCows.add(cow)
        }

        // Track death to cleanup ids
        listeners.add(
            event<EntityDeathEvent> {
                val entity = entity
                if (entity.uniqueId in activeCowIds) {
                    activeCowIds.remove(entity.uniqueId)
                }
            }
        )

        val lastHitTickByCowAndPlayer = mutableMapOf<UUID, MutableMap<UUID, Int>>()

        var tick = 0
        runnables.add(
            repeatingTask(1) {
                // End condition
                if (tick >= durationTicks) {
                    spawnedCows.forEach { if (it.isValid) it.remove() }
                    cancel()
                    return@repeatingTask
                }

                spawnedCows.removeIf { !it.isValid }
                if (spawnedCows.isEmpty()) {
                    cancel()
                    return@repeatingTask
                }

                spawnedCows.forEach { cow ->
                    // Maintain velocity in a straight line
                    cow.velocity = dir.clone().multiply(cowSpeed)

                    // Visuals
                    cow.world.spawnParticle(
                        Particle.CLOUD,
                        cow.location.add(0.0, 0.8, 0.0),
                        2,
                        0.1,
                        0.1,
                        0.1,
                        0.0,
                    )

                    // Damage detection
                    val nearby =
                        cow.world.getNearbyEntities(
                            cow.location,
                            contactRadius,
                            contactRadius,
                            contactRadius,
                        )
                    nearby.forEach { ent ->
                        val victim = (ent as? Player) ?: return@forEach
                        if (victim == player) return@forEach

                        val cowMap =
                            lastHitTickByCowAndPlayer.getOrPut(cow.uniqueId) { mutableMapOf() }
                        val lastHit = cowMap[victim.uniqueId] ?: -9999
                        if (tick - lastHit < hitCooldownTicks) return@forEach

                        // Apply damage and knockback
                        SmashDamageEvent(victim, Damager.DamagerLivingEntity(player), damagePerHit)
                            .callEvent()

                        victim.world.playSound(
                            victim.location,
                            Sound.ENTITY_GENERIC_EXPLODE,
                            0.6f,
                            1f,
                        )
                        victim.world.spawnParticle(
                            Particle.EXPLOSION,
                            victim.location.add(0.0, 0.2, 0.0),
                            1,
                        )

                        // Dedicated melee-style knockback using our multiplier (projectile null)
                        victim.doKnockback(
                            knockbackMultiplier,
                            damagePerHit,
                            (victim as LivingEntity).health,
                            cow.location.toVector(),
                            null,
                        )

                        cowMap[victim.uniqueId] = tick
                    }
                }

                tick++
            }
        )

        player.playSound(player.location, Sound.ENTITY_COW_AMBIENT, 2f, 0.6f)
        super.activate()
    }
}
