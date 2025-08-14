package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent
import gg.flyte.twilight.extension.addY
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Cow
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class AngryHerdAbility(player: Player) : BrawlAbility("angry_herd", player) {

    private val stuckTimeMs = metadata.long("stuckTimeMs") ?: 300L
    private val forceMoveTimeMs = metadata.long("forceMoveTimeMs") ?: 350L
    private val durationMs = metadata.long("durationMs") ?: 2500L
    private val hitboxRadius = metadata.double("hitboxRadius") ?: 2.5
    private val damageCooldownMs = metadata.long("damageCooldownMs") ?: 600L
    private val cowDamage = metadata.double("cowDamage") ?: 5.0
    private val cowAmountRadius = metadata.int("cowAmountRadius") ?: 3
    private val cowAmountHeight = metadata.int("cowAmountHeight") ?: 1
    private val knockback = metadata.double("knockback") ?: 1.25

    private val cowDirections = hashMapOf<Entity, Vector>()
    private val lastCowLocations = hashMapOf<Entity, Location>()
    private val lastMoveTime = hashMapOf<Entity, Long>()
    private val lastDamageTime = hashMapOf<Player, Long>()

    private var herdTask: TwilightRunnable? = null

    override fun teardown() {
        super.teardown()
    }

    override fun activate() {
        super.activate()
        cowDirections.clear()
        lastCowLocations.clear()
        lastMoveTime.clear()
        lastDamageTime.clear()

        val cows = arrayListOf<Entity>()

        repeat(cowAmountHeight) { j ->
            for (i in (1 - cowAmountRadius) until cowAmountRadius) {
                val direction = player.location.direction.clone()
                direction.y = 0.0
                direction.normalize()

                val cowLocation = player.location.clone()
                cowLocation.add(direction)
                cowLocation.add(Vector(-direction.z, 0.0, direction.x).multiply(i * 1.5))
                cowLocation.add(Vector(0, j, 0))

                val cow = player.world.spawn(cowLocation, Cow::class.java)
                cows.add(cow)

                val cowDirection = player.location.direction.clone()
                cowDirection.y = 0.0
                cowDirection.normalize()
                cowDirection.multiply(0.85)
                cowDirection.y = -0.2

                cowDirections[cow] = cowDirection
                lastCowLocations[cow] = cowLocation
                lastMoveTime[cow] = System.currentTimeMillis()
            }
        }

        player.playSound(player.eyeLocation, Sound.ENTITY_COW_AMBIENT, 2f, 0.6f)

        herdTask?.cancel()

        herdTask = repeatingTask(1) {
            if (cows.isEmpty()) {
                cancel()
                return@repeatingTask
            }

            if (getElaspedSinceLastActivation() >= durationMs) {
                cancel()

                cows.removeIf { cow ->
                    if (cow.isValid) {
                        cow.world.spawnParticle(Particle.EXPLOSION, cow.location.add(0.0, 1.0, 0.0), 1, 0)
                    }

                    true
                }

                return@repeatingTask
            }

            cows.removeIf { cow ->
                if (lastCowLocations[cow] != null && cow.location.distance(lastCowLocations[cow]!!) > 1.0) {
                    lastCowLocations[cow] = cow.location
                    lastMoveTime[cow] = System.currentTimeMillis()
                }

                if ((System.currentTimeMillis() - (lastMoveTime[cow] ?: 0)) >= stuckTimeMs) {
                    if (cow.isValid) {
                        cow.world.spawnParticle(Particle.EXPLOSION, cow.location.add(0.0, 1.0, 0.0), 1, 0)
                    }

                    return@removeIf true
                }

                if (cow.isOnGround) {
                    cowDirections[cow]?.let {
                        cowDirections[cow] = it.setY(0.1)
                    }
                } else {
                    cowDirections[cow]?.let {
                        cowDirections[cow] = it.setY(Math.max(-1.0, it.y - 0.03))
                    }
                }

                if (cow.isOnGround && System.currentTimeMillis() - (lastMoveTime[cow] ?: 0) >= forceMoveTimeMs) {
                    cowDirections[cow]?.let {
                        cow.velocity = it.clone().add(Vector(0.0, 0.75, 0.0))
                    }
                } else {
                    cowDirections[cow]?.let {
                        cow.velocity = it
                    }
                }

                if (Math.random() > 0.99) {
                    cow.world.playSound(cow.location, Sound.ENTITY_COW_AMBIENT, 1f, 1f)
                }

                if (Math.random() > 0.97) {
                    cow.world.playSound(cow.location, Sound.ENTITY_COW_STEP, 1f, 1.2f)
                }

                player.world.players
                    .filter { it != player && cow.location.distance(it.location) < hitboxRadius }
                    .forEach {
                        lastDamageTime.putIfAbsent(it, 0L)

                        if ((System.currentTimeMillis() - (lastDamageTime[it] ?: 0)) < damageCooldownMs) {
                            return@forEach
                        }

                        lastDamageTime[it] = System.currentTimeMillis()

                        val damageEvent = SmashDamageEvent(it, Damager.DamagerLivingEntity(player), cowDamage).apply {
                            knockbackMultiplier = knockback
                        }

                        damageEvent.callEvent()
                        cow.world.spawnParticle(Particle.EXPLOSION, cow.location.addY(1.0), 1, 0)
                        cow.world.playSound(cow.location, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.75f, 0.8f)
                        cow.world.playSound(cow.location, Sound.ENTITY_COW_HURT, 1.5f, 0.75f)
                    }

                false
            }
        }
    }
}
