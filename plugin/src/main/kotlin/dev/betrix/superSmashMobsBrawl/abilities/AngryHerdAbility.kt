package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent
import dev.betrix.superSmashMobsBrawl.extensions.blocksPerSecond
import dev.betrix.superSmashMobsBrawl.interfaces.MetadataAccessor
import gg.flyte.twilight.extension.addY
import gg.flyte.twilight.extension.getNearbyEntities
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Cow
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class AngryHerdCow(
    initialLocation: Location,
    private val owner: Player,
    metadata: MetadataAccessor,
) {

    private val durationTicks = metadata.int("durationTicks") ?: 50
    private val stuckVelocityThreshold = metadata.double("stuckVelocityThreshold") ?: 0.25
    private val stuckTimeMs = metadata.long("stuckTimeMs") ?: 1000L
    private val hitboxRadius = metadata.double("contactRadius") ?: 1.2
    private val damageCooldownMs = (metadata.int("hitCooldownTicks")?.times(50)) ?: 500
    private val knockback = metadata.double("knockbackMultiplier") ?: 1.25
    private val cowDamage = metadata.double("damagePerHit") ?: 5.0
    private val cowSpeed = metadata.double("cowSpeed") ?: 0.9

    private val forwardDirection = owner.location.direction.clone().setY(0.0).normalize()

    val entity: Cow =
        owner.world.spawn(initialLocation, Cow::class.java).apply {
            setAI(false)
            // Face the movement direction and start moving forward immediately
            location.direction = forwardDirection
            velocity = forwardDirection.clone().multiply(cowSpeed)
        }

    private var lastTimeAboveVelocityThreshold = System.currentTimeMillis()
    private var lastDamageTime = 0L

    val task =
        repeatingTask(1) {
            val currentTime = System.currentTimeMillis()

            if (!owner.isOnline) {
                teardown()
                return@repeatingTask
            }

            if (entity.ticksLived > durationTicks) {
                teardown()
                return@repeatingTask
            }

            if (entity.blocksPerSecond < stuckVelocityThreshold) {
                if (lastTimeAboveVelocityThreshold + stuckTimeMs < currentTime) {
                    teardown()
                    return@repeatingTask
                }

                if (entity.isOnGround) {
                    // Nudge upward to help get over small obstacles
                    entity.velocity = entity.velocity.clone().add(Vector(0.0, 0.75, 0.0))
                }
            } else {
                lastTimeAboveVelocityThreshold = currentTime
            }

            // Maintain forward horizontal velocity; preserve current vertical velocity
            val currentVel = entity.velocity
            entity.velocity =
                Vector(forwardDirection.x * cowSpeed, currentVel.y, forwardDirection.z * cowSpeed)

            doAmbience()
            tryDamagePlayers()
        }

    fun teardown() {
        if (entity.isValid) {
            entity.world.spawnParticle(Particle.EXPLOSION, entity.location.add(0.0, 1.0, 0.0), 1)
            entity.remove()
        }

        task.cancel()
    }

    private fun doAmbience() {
        if (Math.random() > 0.99) {
            entity.world.playSound(entity.location, Sound.ENTITY_COW_AMBIENT, 1f, 1f)
        }

        if (Math.random() > 0.97) {
            entity.world.playSound(entity.location, Sound.ENTITY_COW_STEP, 1f, 1.2f)
        }
    }

    private fun tryDamagePlayers() {
        val currentTime = System.currentTimeMillis()

        entity
            .getNearbyEntities(hitboxRadius)
            .filterIsInstance<Player>()
            .filter { it != owner }
            .forEach { livingEntity ->
                if (currentTime - lastDamageTime < damageCooldownMs) {
                    return@forEach
                }

                lastDamageTime = currentTime

                val damageEvent =
                    SmashDamageEvent(livingEntity, Damager.DamagerLivingEntity(owner), cowDamage)
                        .apply { knockbackMultiplier = knockback }

                damageEvent.callEvent()
                entity.world.spawnParticle(Particle.EXPLOSION, entity.location.addY(1.0), 1)
                entity.world.playSound(
                    entity.location,
                    Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR,
                    0.75f,
                    0.8f,
                )
                entity.world.playSound(entity.location, Sound.ENTITY_COW_HURT, 1.5f, 0.75f)
            }
    }
}

class AngryHerdAbility(player: Player) : BrawlAbility("angry_herd", player) {

    private val cowAmountRadius = metadata.int("cowAmountRadius") ?: 3
    private val cowAmountHeight = metadata.int("cowAmountHeight") ?: 1
    private val cows = arrayListOf<AngryHerdCow>()

    private fun clearCows() {
        cows.forEach { cow -> cow.teardown() }

        cows.clear()
    }

    override fun teardown() {
        clearCows()
        super.teardown()
    }

    override fun activate() {
        super.activate()
        clearCows()

        repeat(cowAmountHeight) { j ->
            for (i in (1 - cowAmountRadius) until cowAmountRadius) {
                val direction = player.location.direction.clone().setY(0.0).normalize()

                val cowLocation =
                    player.location
                        .clone()
                        .add(direction)
                        .add(Vector(-direction.z, 0.0, direction.x).multiply(i * 1.5))
                        .add(Vector(0, j, 0))

                cows.add(AngryHerdCow(cowLocation, player, metadata))
            }
        }

        player.playSound(player.eyeLocation, Sound.ENTITY_COW_AMBIENT, 2f, 0.6f)
    }
}
