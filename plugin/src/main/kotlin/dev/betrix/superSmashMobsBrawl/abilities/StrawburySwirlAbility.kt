package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.events.BrawlDamageEvent
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.services.ComboTrackerService
import gg.flyte.twilight.extension.addY
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class StrawburySwirlAbility(player: Player) : BrawlAbility("strawbury_swirl", player), KoinComponent {
    private val comboTracker: ComboTrackerService by inject()

    private val spiralDurationMs = metadata.long("spiralDurationMs") ?: 2000
    private val velocityDurationMs = metadata.long("velocityDurationMs") ?: 1200
    private val hitboxRadius = metadata.double("hitboxRadius") ?: 1.8
    private val baseDamage = metadata.double("baseDamage") ?: 4.0
    private val damagePerCombo = metadata.double("damagePerCombo") ?: 0.75
    private val maxTimesHit = metadata.int("maxTimesHit") ?: 1
    private val damageCooldownMs = metadata.long("damageCooldownMs") ?: 250
    private val particleSpiralRadius = metadata.double("particleSpiralRadius") ?: 0.9

    private val lastDamageTime = hashMapOf<Player, Long>()
    private val timesHit = hashMapOf<Player, Int>()

    private var spiralRunnable: TwilightRunnable? = null

    override fun activate() {
        super.activate()

        spiralRunnable?.cancel()

        val direction = player.location.direction
        val spiralLocation =
            player.location.clone().add(Vector(0.0, 1.0, 0.0)).add(direction.clone().multiply(2))
        var doVelocity = true
        var first = true

        spiralRunnable =
            repeatingTask(1) {
                if (elapsedSinceLastActivation >= spiralDurationMs) {
                    cancel()
                    return@repeatingTask
                }

                if (player.isSneaking || elapsedSinceLastActivation >= velocityDurationMs) {
                    doVelocity = false
                }

                if (doVelocity) {
                    player.velocity =
                        direction.clone().add(Vector(0.0, 0.1, 0.0)).normalize().multiply(0.45)
                }

                val oldLocation = spiralLocation.clone()
                val totalDistance = 0.6

                spiralLocation.add(direction.clone().multiply(totalDistance))

                val circleFirst = Vector(-direction.z, 0.0, direction.x).normalize()
                val circleSecond = direction.clone().crossProduct(circleFirst).normalize()

                val speed = 3
                var theta = player.ticksLived.toDouble() / speed
                var totalAddedDistance = 0.0

                while (totalAddedDistance < totalDistance) {
                    val firstParticle =
                        oldLocation
                            .clone()
                            .add(
                                getCirclePoint(
                                    circleFirst,
                                    circleSecond,
                                    theta,
                                    particleSpiralRadius,
                                )
                            )
                    val secondParticle =
                        oldLocation
                            .clone()
                            .add(
                                getCirclePoint(
                                    circleFirst,
                                    circleSecond,
                                    theta + Math.PI,
                                    particleSpiralRadius,
                                )
                            )

                    if (first) {
                        firstParticle.world.playSound(
                            firstParticle,
                            Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED,
                            0.15f,
                            1.0f,
                        )
                        secondParticle.world.playSound(
                            secondParticle,
                            Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED,
                            0.15f,
                            1.0f,
                        )
                        first = false
                    }

                    Particle.CHERRY_LEAVES.builder()
                        .location(firstParticle)
                        .count(1)
                        .extra(0.0)
                        .receivers(96, true)
                        .spawn()

                    Particle.CHERRY_LEAVES.builder()
                        .location(secondParticle)
                        .count(1)
                        .extra(0.0)
                        .receivers(96, true)
                        .spawn()

                    val distance = totalDistance / 4
                    oldLocation.add(direction.clone().multiply(distance))
                    theta += (1.0 / speed) * (distance / totalDistance)
                    totalAddedDistance += distance
                }

                player.world.players
                    .filter { it != player }
                    .forEach {
                        if (
                            lastDamageTime[it] != null &&
                                System.currentTimeMillis() - lastDamageTime[it]!! < damageCooldownMs
                        ) {
                            return@forEach
                        }

                        if (timesHit[it] != null && timesHit[it]!! >= maxTimesHit) {
                            return@forEach
                        }

                        if (it.eyeLocation.distance(spiralLocation) >= hitboxRadius) {
                            return@forEach
                        }

                        lastDamageTime[it] = System.currentTimeMillis()
                        timesHit.putIfAbsent(it, 0)
                        timesHit[it] = timesHit[it]!! + 1

                        val comboCount = comboTracker.getCombo(player, it)
                        val totalDamage = baseDamage + (comboCount * damagePerCombo)

                        Particle.CHERRY_LEAVES.builder()
                            .location(it.location.addY(1.0))
                            .offset(0.3, 0.3, 0.3)
                            .count(30)
                            .extra(0.4)
                            .receivers(96, true)
                            .spawn()
                        it.world.playSound(
                            it.eyeLocation,
                            Sound.BLOCK_CHERRY_LEAVES_HIT,
                            0.3f,
                            1.5f,
                        )

                        BrawlDamageEvent(it, Damager.DamagerLivingEntity(player), totalDamage)
                            .callEvent()
                    }
            }

        spiralRunnable?.let { runnables.add(it) }
    }

    override fun teardown() {
        spiralRunnable?.cancel()
        spiralRunnable = null
        super.teardown()
    }

    private fun getCirclePoint(
        circleFirst: Vector,
        circleSecond: Vector,
        theta: Double,
        radius: Double,
    ): Vector {
        val particleOffset = circleFirst.clone().multiply(Math.cos(theta) * radius)
        particleOffset.add(circleSecond.clone().multiply(Math.sin(theta) * radius))

        return particleOffset
    }
}

