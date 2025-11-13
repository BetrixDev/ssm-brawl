package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.events.BrawlDamageEvent
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageType
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.extensions.event
import dev.betrix.superSmashMobsBrawl.extensions.isOnBlock
import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import dev.betrix.superSmashMobsBrawl.extensions.setVelocity
import gg.flyte.twilight.event.event
import gg.flyte.twilight.extension.getNearbyEntities
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import kotlin.random.Random
import org.bukkit.Effect
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.BlockFace
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class WaterSplashAbility(player: Player) : BrawlAbility("water_splash", player) {

    private val minimumAirTimeMs: Long = metadata.long("minimumAirTimeMs") ?: 750L
    private val secondBoostTimeMs: Long = metadata.long("secondBoostTimeMs") ?: 800L
    private val firstVelocity: Double = metadata.double("firstVelocity") ?: 1.0
    private val secondVelocity: Double = metadata.double("secondVelocity") ?: 1.5
    private val pullInRadius: Double = metadata.double("pullInRadius") ?: 5.0
    private val damageRadius: Double = metadata.double("damageRadius") ?: 5.0
    private val damage: Double = metadata.double("damage") ?: 12.0
    private val pullYStrength: Double = metadata.double("pullYStrength") ?: 0.5
    private val knockbackMultiplier: Double = metadata.double("knockbackMultiplier") ?: 1.0
    private val blockEffectRadius: Int = (metadata.int("blockEffectRadius") ?: 5).coerceAtLeast(0)
    private val blockEffectChance: Double = metadata.double("blockEffectChance") ?: 0.1

    private var splashTask: TwilightRunnable? = null
    private var boostUsed: Boolean = false

    override fun activate() {
        super.activate()

        // Initial upward velocity
        player.setVelocity(
            velocity = Vector(0.0, 1.0, 0.0),
            strength = firstVelocity,
            ySet = true,
            yBase = firstVelocity,
            yAdd = 0.0,
            yMax = 10.0,
            groundBoost = false,
        )

        // Pull nearby players towards owner
        pullNearbyPlayers()

        // Start the splash monitoring task
        boostUsed = false
        startSplashTask()
    }

    override fun teardown() {
        splashTask?.cancel()
        splashTask = null
        super.teardown()
    }

    private fun pullNearbyPlayers() {
        player.location
            .getNearbyEntities(pullInRadius, pullInRadius, pullInRadius)
            .filterIsInstance<LivingEntity>()
            .filter { it != player }
            .forEach { entity ->
                val trajectory =
                    player.location.toVector().subtract(entity.location.toVector()).normalize()
                trajectory.y = pullYStrength

                entity.setVelocity(
                    velocity = trajectory,
                    strength = 1.0,
                    ySet = false,
                    yBase = 0.0,
                    yAdd = 0.0,
                    yMax = 10.0,
                    groundBoost = false,
                )
            }
    }

    private fun startSplashTask() {
        splashTask?.cancel()

        val activationTime = elapsedSinceLastActivation

        splashTask =
            repeatingTask(0, 1) {
                if (!player.isValid || player.isDead) {
                    cancel()
                    splashTask = null
                    return@repeatingTask
                }

                // Show water particles
                player.world.spawnParticle(
                    Particle.DRIPPING_WATER,
                    player.location,
                    10,
                    0.5,
                    0.5,
                    0.5,
                    0.01,
                )

                val timeElapsed = System.currentTimeMillis() - (lastUsed - activationTime)

                // Check for landing
                if (player.isOnBlock() && timeElapsed >= minimumAirTimeMs) {
                    performSplash()
                    cancel()
                    splashTask = null
                    return@repeatingTask
                }

                // Check for second boost
                if (player.isBlocking && timeElapsed >= secondBoostTimeMs && !boostUsed) {
                    boostUsed = true
                    player.sendDebugMessage("[WaterSplash] Second boost activated")

                    val direction = player.location.direction.multiply(secondVelocity)
                    player.setVelocity(
                        velocity = direction,
                        strength = 1.0,
                        ySet = false,
                        yBase = 0.0,
                        yAdd = 0.0,
                        yMax = 10.0,
                        groundBoost = false,
                    )
                }
            }

        splashTask?.let { runnables.add(it) }
    }

    private fun performSplash() {
        val ownerLocation = player.location

        player.sendDebugMessage(
            "[WaterSplash] Landing splash at ${ownerLocation.x}, ${ownerLocation.y}, ${ownerLocation.z}"
        )

        // Firework particles
        player.world.spawnParticle(Particle.FIREWORK, player.eyeLocation, 50, 0.0, 0.0, 0.0, 0.5)

        // Block effects
        triggerBlockEffects()

        // Sound effect
        player.world.playSound(ownerLocation, Sound.ENTITY_GENERIC_SPLASH, 2f, 0f)

        // Damage nearby entities
        ownerLocation
            .getNearbyEntities(damageRadius, damageRadius, damageRadius)
            .filterIsInstance<LivingEntity>()
            .filter { it != player }
            .forEach { target ->
                val distance = target.location.distance(ownerLocation)
                val normalizedDistance =
                    ((damageRadius - distance) / damageRadius).coerceAtLeast(0.0)
                val scaledDamage = damage * normalizedDistance

                player.sendDebugMessage("[WaterSplash] Damaging ${target.name} for $scaledDamage")

                BrawlDamageEvent(
                        target,
                        Damager.DamagerLivingEntity(player),
                        scaledDamage,
                        knockbackMultiplier,
                        BrawlDamageType.Explosion,
                    )
                    .callEvent()
            }
    }

    private fun triggerBlockEffects() {
        if (blockEffectRadius <= 0 || blockEffectChance <= 0.0) {
            return
        }

        val origin = player.location.block
        val world = origin.world

        for (x in -blockEffectRadius..blockEffectRadius) {
            for (y in -blockEffectRadius..blockEffectRadius) {
                for (z in -blockEffectRadius..blockEffectRadius) {
                    if (Random.nextDouble() >= blockEffectChance) {
                        continue
                    }

                    val checkBlock = origin.getRelative(x, y, z)

                    if (!checkBlock.type.isSolid || checkBlock.type == Material.AIR) {
                        continue
                    }

                    val aboveBlock = checkBlock.getRelative(BlockFace.UP)

                    if (aboveBlock.type.isSolid) {
                        continue
                    }

                    world.playEffect(checkBlock.location, Effect.STEP_SOUND, checkBlock.type)
                }
            }
        }
    }

    override fun setup() {
        super.setup()

        // Listen for damage events to cancel knockback while ability is active
        listeners.add(
            event<BrawlDamageEvent> {
                if (victim != this@WaterSplashAbility.player) {
                    return@event
                }

                if (splashTask != null) {
                    player.sendDebugMessage("[WaterSplash] Cancelling knockback during splash")
                    knockbackMultiplier = 0.0
                }
            }
        )
    }
}
