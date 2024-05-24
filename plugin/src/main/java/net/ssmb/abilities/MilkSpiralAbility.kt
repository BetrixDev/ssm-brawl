package net.ssmb.abilities

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import net.ssmb.SSMB
import net.ssmb.dtos.minigame.MinigameStartSuccess
import net.ssmb.events.BrawlDamageEvent
import net.ssmb.events.BrawlDamageType
import net.ssmb.extensions.getMetadata
import net.ssmb.utils.TaggedKeyBool
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class MilkSpiralAbility(
    private val player: Player,
    abilityEntry: MinigameStartSuccess.PlayerData.KitData.AbilityEntry
) : SsmbAbility(player, abilityEntry) {
    private val plugin = SSMB.instance

    private val spiralDurationMs = getMetaInt("spiral_duration_ms", 3000)
    private val velocityDurationMs = getMetaInt("velocity_duration_ms", 1800)
    private val damageCooldownMs = getMetaInt("damage_cooldown_ms", 250).toLong()
    private val maxTimesHit = getMetaInt("max_times_hit", 2)
    private val hitboxRadius = getMetaInt("hitbox_radius", 2)
    private val damage = getMetaDouble("damage", 5.0)
    private val spiralRadius = getMetaDouble("spiral_radius", 1.5)

    private var spiralJob: Job? = null
    private val timesHit = hashMapOf<Player, Int>()
    private val lastDamageTime = hashMapOf<Player, Long>()

    override fun doAbility() {
        val spiralStartTimeMs = System.currentTimeMillis()

        timesHit.clear()
        lastDamageTime.clear()

        val direction = player.location.direction
        val spiralLocation =
            player.location.add(Vector(0.0, 1.0, 0.0)).add(direction.clone().multiply(2.0))
        var doVelocity = true

        spiralJob =
            plugin.launch {
                while (true) {
                    val currentTime = System.currentTimeMillis()

                    if (currentTime - spiralStartTimeMs >= spiralDurationMs) {
                        spiralJob?.cancel()
                        return@launch
                    }

                    if (
                        player.isSneaking || currentTime - spiralStartTimeMs >= velocityDurationMs
                    ) {
                        doVelocity = false
                    }

                    val oldLocation = spiralLocation.clone()
                    val totalDistance = 0.7
                    spiralLocation.add(direction.clone().multiply(totalDistance))

                    // Make the first vector that represents the x-axis of the circle in the spiral
                    // Set y to 0 and rotate 90 degrees to get the middle part of the circle on the
                    // spiral
                    val firstCircle = Vector(-direction.z, 0.0, direction.x).normalize()
                    // Cross product gets the perpendicular vector which is the y-axis of the circle
                    val secondCircle = direction.clone().crossProduct(firstCircle).normalize()
                    // Now that we have our circle we can make the particles
                    val speed = 3
                    var theta = (player.ticksLived / speed).toDouble()
                    var first = true
                    var totalAddedDistance = 0.0

                    while (totalAddedDistance < totalDistance) {
                        val firstParticle =
                            oldLocation
                                .clone()
                                .add(getCirclePoint(firstCircle, secondCircle, theta, spiralRadius))
                        val secondParticle =
                            oldLocation
                                .clone()
                                .add(
                                    getCirclePoint(
                                        firstCircle,
                                        secondCircle,
                                        theta + Math.PI,
                                        spiralRadius
                                    )
                                )

                        if (first) {
                            firstParticle.world.playSound(
                                firstParticle,
                                Sound.ENTITY_GENERIC_SPLASH,
                                0.2f,
                                0.75f
                            )
                            secondParticle.world.playSound(
                                secondParticle,
                                Sound.ENTITY_GENERIC_SPLASH,
                                0.2f,
                                0.75f
                            )
                            first = false
                        }

                        firstParticle.world.spawnParticle(
                            Particle.FIREWORKS_SPARK,
                            firstParticle,
                            1
                        )
                        secondParticle.world.spawnParticle(
                            Particle.FIREWORKS_SPARK,
                            secondParticle,
                            1
                        )

                        val distance = totalDistance / 4
                        oldLocation.add(direction.clone().multiply(distance))
                        // Multiply the normal theta change by the percent distance moved so we can
                        // simulate the in between
                        theta += (1 / speed) * (distance / totalDistance)
                        totalAddedDistance += distance
                    }

                    player.world.players.forEach { plr ->
                        if (plr == player) {
                            return@forEach
                        }

                        if (plr.getMetadata(TaggedKeyBool("can_take_damage")) == false) {
                            return@forEach
                        }

                        if (
                            lastDamageTime[plr] != null &&
                                currentTime - lastDamageTime[plr]!! < damageCooldownMs
                        ) {
                            return@forEach
                        }

                        if (timesHit[plr] != null && timesHit[plr]!! >= maxTimesHit) {
                            return@forEach
                        }

                        if (
                            plr.location.add(0.0, 1.5, 0.0).distance(spiralLocation) >= hitboxRadius
                        ) {
                            return@forEach
                        }

                        lastDamageTime[plr] = currentTime
                        timesHit[plr] = (timesHit[plr] ?: 0) + 1
                        plr.world.spawnParticle(Particle.FIREWORKS_SPARK, plr.location, 1)
                        plr.world.playSound(plr.location, Sound.ENTITY_GENERIC_SPLASH, 0.2f, 2f)

                        val brawlDamageEvent =
                            BrawlDamageEvent(plr, player, damage, BrawlDamageType.PROJECTILE)
                        brawlDamageEvent.callEvent()
                    }

                    delay(1.ticks)
                }
            }
    }

    private fun getCirclePoint(
        firstCircle: Vector,
        secondCircle: Vector,
        theta: Double,
        radius: Double
    ): Vector {
        val particleOffset = firstCircle.clone().multiply(cos(theta) * radius)
        particleOffset.add(secondCircle.clone().multiply(sin(theta) * radius))

        return particleOffset
    }
}
