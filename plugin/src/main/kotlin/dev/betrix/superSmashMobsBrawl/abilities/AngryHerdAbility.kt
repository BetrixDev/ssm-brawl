package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent
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

    private var herdTask: TwilightRunnable? = null
    private val cowDirections = mutableMapOf<Entity, Vector>()
    private val lastCowLocation = mutableMapOf<Entity, Location>()
    private val lastMoveTime = mutableMapOf<Entity, Long>()
    private val lastDamageTime = mutableMapOf<Player, Long>()

    private val stuckTimeMs: Long = metadata.long("stuckTimeMs") ?: 300
    private val forceMoveTimeMs: Long = metadata.long("forceMoveTimeMs") ?: 350
    private val durationMs: Long = metadata.long("durationMs") ?: 2500
    private val hitboxRadius: Double = metadata.double("hitboxRadius") ?: 2.2
    private val damageCooldownMs: Long = metadata.long("damageCooldownMs") ?: 600
    private val damage: Double = metadata.double("damage") ?: 5.0
    private val cowAmountRadius: Int = metadata.int("cowAmountRadius") ?: 3
    private val cowAmountHeight: Int = metadata.int("cowAmountHeight") ?: 1
    private val knockback: Double = metadata.double("knockback") ?: 1.25

    override fun teardown() {
        super.teardown()
        herdTask?.cancel()
        herdTask = null
    }

    override fun activate() {
        super.activate()
        cowDirections.clear()
        lastCowLocation.clear()
        lastMoveTime.clear()
        lastDamageTime.clear()

        val cows = mutableListOf<Entity>()

        for (j in 0 until cowAmountHeight) {
            for (i in (1 - cowAmountRadius) until cowAmountRadius) {
                val direction =
                    player.location.direction.apply {
                        y = 0.0
                        normalize()
                    }

                val cowLocation =
                    player.location.clone().apply {
                        add(direction)
                        add(Vector(-direction.z, 0.0, direction.x).multiply(i * 1.5))
                        add(Vector(0.0, j.toDouble(), 0.0))
                    }

                val cow = player.world.spawn(cowLocation, Cow::class.java)
                cows.add(cow)

                val cowDirection =
                    player.location.direction.apply {
                        y = 0.0
                        normalize()
                        multiply(0.75)
                        y = -0.2
                    }

                cowDirections[cow] = cowDirection
                lastCowLocation[cow] = cowLocation
                lastMoveTime[cow] = System.currentTimeMillis()
            }
        }

        player.world.playSound(player.location, Sound.ENTITY_COW_AMBIENT, 2f, 0.6f)

        herdTask?.cancel()
        herdTask = null

        herdTask =
            repeatingTask(1) task@{
                // End condition: owner null or ability duration elapsed
                if (elapsedSinceLastActivation >= durationMs) {
                    // Cleanup cows
                    cows.forEach { cow ->
                        if (cow.isValid) {
                            Particle.EXPLOSION.builder()
                                .location(cow.location.clone().add(0.0, 1.0, 0.0))
                                .count(1)
                                .receivers(96, true)
                                .spawn()
                            cow.remove()
                        }
                    }
                    // Stop this task
                    this.cancel()
                    herdTask = null
                    return@task
                }

                cows.forEach cowLoop@{ cow ->
                    val lastLoc = lastCowLocation[cow]
                    if (lastLoc != null && cow.location.distance(lastLoc) > 1) {
                        lastCowLocation[cow] = cow.location
                        lastMoveTime[cow] = System.currentTimeMillis()
                    }

                    if (System.currentTimeMillis() - (lastMoveTime[cow] ?: 0) >= stuckTimeMs) {
                        if (cow.isValid) {
                            Particle.EXPLOSION.builder()
                                .location(cow.location.clone().add(0.0, 1.0, 0.0))
                                .count(1)
                                .receivers(96, true)
                                .spawn()
                            cow.remove()
                        }
                        return@cowLoop
                    }

                    val currentDir = cowDirections[cow] ?: return@cowLoop
                    if (cow.isOnGround) {
                        cowDirections[cow] = currentDir.setY(-0.1)
                    } else {
                        cowDirections[cow] = currentDir.setY(maxOf(-1.0, currentDir.y - 0.03))
                    }

                    if (
                        cow.isOnGround &&
                        System.currentTimeMillis() - (lastMoveTime[cow] ?: 0) >= forceMoveTimeMs
                    ) {
                        cow.velocity =
                            cowDirections[cow]?.clone()?.add(Vector(0.0, 0.75, 0.0)) ?: Vector()
                    } else {
                        cow.velocity = cowDirections[cow] ?: Vector()
                    }

                    if (Math.random() > 0.99) {
                        cow.world.playSound(cow.location, Sound.ENTITY_COW_AMBIENT, 1f, 1f)
                    }
                    if (Math.random() > 0.97) {
                        cow.world.playSound(cow.location, Sound.ENTITY_COW_STEP, 1f, 1.2f)
                    }

                    player.world.players.forEach playerLoop@{
                        if (it == player) return@playerLoop
                        //                    if (!DamageUtil.canDamage(it, player))
                        // return@playerLoop
                        if (cow.location.distance(it.location) >= hitboxRadius) return@playerLoop

                        val lastHit = lastDamageTime[it] ?: 0L
                        if (System.currentTimeMillis() - lastHit < damageCooldownMs)
                            return@playerLoop

                        lastDamageTime[it] = System.currentTimeMillis()

                        SmashDamageEvent(it, Damager.DamagerLivingEntity(player), damage).apply {
                            knockbackMultiplier *= knockback
                            //                        isIgnoreDamageDelay = true
                            //                        reason = name
                            callEvent()
                        }

                        Particle.EXPLOSION.builder()
                            .location(cow.location.clone().add(0.0, 1.0, 0.0))
                            .offset(1.0, 1.0, 1.0)
                            .count(12)
                            .receivers(96, true)
                            .spawn()
                        cow.world.playSound(
                            cow.location,
                            Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR,
                            0.75f,
                            0.8f,
                        )
                        cow.world.playSound(cow.location, Sound.ENTITY_COW_HURT, 1.5f, 0.75f)
                    }
                }
            }

        herdTask?.let { runnables.add(it) }
    }
}
