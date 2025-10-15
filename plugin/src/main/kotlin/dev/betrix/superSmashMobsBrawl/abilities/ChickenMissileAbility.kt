package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.events.BrawlDamageEvent
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageType
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.extensions.setVelocity
import gg.flyte.twilight.extension.getNearbyEntities
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.*
import org.bukkit.entity.Chicken
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

class ChickenMissileAbility(player: Player) : BrawlAbility("chicken_missile", player) {
    private val damage = metadata.double("damage") ?: 8.0
    private val explosionRadius = metadata.double("explosionRadius") ?: 3.0
    private val directHitboxRadius = metadata.double("directHitboxRadius") ?: 2.0
    private val minimumHitTimeMs = metadata.long("minimumHitTimeMs") ?: 200L
    private val durationMs = metadata.long("durationMs") ?: 4000L
    private val velocityStrength = metadata.double("velocityStrength") ?: 0.6
    private val knockbackStrength = metadata.double("knockbackStrength") ?: 1.6
    private val knockbackYAdd = metadata.double("knockbackYAdd") ?: 0.8
    private val knockbackYMax = metadata.double("knockbackYMax") ?: 10.0

    override fun activate() {
        super.activate()

        val chicken = player.world.spawn(
            player.eyeLocation.add(player.location.direction),
            Chicken::class.java
        ).apply {
            setBaby()
            ageLock = true
        }

        val direction = player.location.direction.multiply(velocityStrength)
        var lastLocation: Location? = null
        val activationTime = elapsedSinceLastActivation

        runnables.add(
            repeatingTask(0) {
                if (!chicken.isValid || chicken.isDead) {
                    cancel()
                    return@repeatingTask
                }

                chicken.velocity = direction
                chicken.world.playSound(chicken.location, Sound.ENTITY_CHICKEN_HURT, 0.3f, 1.5f)

                val elapsedTime = System.currentTimeMillis() - (lastUsed - activationTime)

                if (elapsedTime < minimumHitTimeMs) {
                    return@repeatingTask
                }

                var shouldDetonate = false

                if (elapsedTime >= durationMs) {
                    shouldDetonate = true
                } else {
                    val nearbyEntities = chicken.getNearbyEntities(directHitboxRadius)
                        .filterIsInstance<LivingEntity>()
                        .filter { it != player && it != chicken }

                    for (entity in nearbyEntities) {
                        if (canDamageEntity(entity)) {
                            shouldDetonate = true
                            break
                        }
                    }

                    if (lastLocation != null && lastLocation!!.distance(chicken.location) < 0.2 || chicken.isOnGround) {
                        shouldDetonate = true
                    }
                    lastLocation = chicken.location.clone()
                }

                if (shouldDetonate) {
                    detonateChicken(chicken)
                    cancel()
                }
            }
        )
    }

    private fun detonateChicken(chicken: Chicken) {
        val explosionLocation = chicken.location

        chicken.getNearbyEntities(explosionRadius)
            .filterIsInstance<LivingEntity>()
            .filter { it != player && it != chicken && canDamageEntity(it) }
            .forEach { entity ->
                BrawlDamageEvent(
                    entity,
                    Damager.DamagerLivingEntity(player),
                    damage,
                    knockbackMultiplier = 0.0,
                    damageType = BrawlDamageType.Explosion,
                )
//                    .apply {
//                    setIgnoreDamageDelay(true)
//                }
                    .callEvent()

                val living2d = entity.location.toVector().setY(0.0)
                val chicken2d = explosionLocation.toVector().setY(0.0)
                val knockbackDirection = living2d.subtract(chicken2d).normalize()

                entity.setVelocity(
                    knockbackDirection,
                    knockbackStrength,
                    false,
                    0.0,
                    knockbackYAdd,
                    knockbackYMax,
                    true
                )
            }

        explosionLocation.world.spawnParticle(
            Particle.EXPLOSION,
            explosionLocation,
            1,
            0.0,
            0.0,
            0.0,
            0.0
        )

        explosionLocation.world.playSound(explosionLocation, Sound.ENTITY_GENERIC_EXPLODE, 2f, 1.2f)

        spawnFirework(explosionLocation.clone().add(0.0, 0.6, 0.0))

        chicken.remove()
    }

    private fun spawnFirework(location: Location) {
        val firework = location.world.spawn(location, org.bukkit.entity.Firework::class.java)
        val fireworkMeta = firework.fireworkMeta

        fireworkMeta.addEffect(
            FireworkEffect.builder()
                .with(FireworkEffect.Type.BALL)
                .withColor(Color.WHITE)
                .build()
        )

        fireworkMeta.power = 0
        firework.fireworkMeta = fireworkMeta

        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            firework.detonate()
        }, 1L)
    }

    private fun canDamageEntity(entity: LivingEntity): Boolean {
        if (entity !is Player) {
            return true
        }

        val minigame = minigameService.getMinigameForPlayer(player) ?: return false
        return !minigame.arePlayersOnSameTeam(player, entity)
    }
}
