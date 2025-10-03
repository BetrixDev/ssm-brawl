package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.events.BrawlDamageEvent
import dev.betrix.superSmashMobsBrawl.events.BrawlDeathEvent
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageType
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.extensions.event
import dev.betrix.superSmashMobsBrawl.extensions.setVelocity
import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Bat
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.util.Vector

class BatWaveAbility(player: Player) : BrawlAbility("bat_wave", player) {

    private val batCount = metadata.int("batCount") ?: 32
    private val batDurationMs = metadata.long("batDurationMs") ?: 2500
    private val batSpeed = metadata.double("batSpeed") ?: 0.75
    private val batDamage = metadata.double("batDamage") ?: 3.0
    private val hitboxRadius = metadata.double("hitboxRadius") ?: 2.0
    private val knockbackMultiplier = metadata.double("knockbackMultiplier") ?: 1.75
    private val disableDamageThreshold = metadata.double("disableDamageThreshold") ?: 20.0
    private val batHitCooldownMs = metadata.long("batHitCooldownMs") ?: 200
    private val leashCooldownMs = metadata.long("leashCooldownMs") ?: 500
    private val pullStrength = metadata.double("pullStrength") ?: 0.35

    private var batTask: TwilightRunnable? = null
    private var startLocation: Location? = null
    private var startTime: Long = 0
    private var lastLeashTime: Long = 0
    private var damageTaken: Double = 0.0
    private var lastBatHitTime: Long = 0
    private val bats = mutableListOf<Bat>()

    override fun setup() {
        super.setup()

        listeners.add(
            event<BrawlDamageEvent> {
                if (victim !is Player || victim != player) {
                    return@event
                }

                damageTaken += damage

                if (damageTaken >= disableDamageThreshold) {
                    clearBats()
                }
            }
        )

        listeners.add(
            BrawlDeathEvent.listen {
                if (player != this@BatWaveAbility.player) {
                    return@listen
                }

                clearBats()
            }
        )
    }

    override fun onPlayerInteract(event: PlayerInteractEvent) {
        if (batTask != null) {
            if (System.currentTimeMillis() - lastLeashTime < leashCooldownMs) {
                return
            }

            lastLeashTime = System.currentTimeMillis()

            if (bats.isEmpty()) {
                return
            }

            val shouldLeash = !bats.first().isLeashed
            if (shouldLeash) {
                leashBats()
            } else {
                unleashBats()
            }
            return
        }

        super.onPlayerInteract(event)
    }

    override fun activate() {
        super.activate()

        clearBats()
        startLocation = player.eyeLocation.clone()
        startTime = System.currentTimeMillis()
        damageTaken = 0.0

        for (i in 0 until batCount) {
            val bat = player.world.spawn(player.eyeLocation, Bat::class.java)
            bats.add(bat)
        }

        batTask =
            repeatingTask(0) {
                if (System.currentTimeMillis() - startTime >= batDurationMs) {
                    clearBats()
                    cancel()
                    return@repeatingTask
                }

                val batVector = Vector(0.0, 0.0, 0.0)
                var batCount = 0.0

                for (bat in bats) {
                    if (!bat.isValid) {
                        continue
                    }

                    batVector.add(bat.location.toVector())
                    batCount++

                    val random =
                        Vector(
                            (Math.random() - 0.5) / 2,
                            (Math.random() - 0.5) / 2,
                            (Math.random() - 0.5) / 2,
                        )

                    val batVelocity =
                        startLocation!!.direction.clone().multiply(batSpeed).add(random)
                    bat.velocity = batVelocity

                    for (otherPlayer in bat.world.players) {
                        if (System.currentTimeMillis() - lastBatHitTime < batHitCooldownMs) {
                            break
                        }

                        if (otherPlayer == player) {
                            continue
                        }

                        val minigame = minigameService.getMinigameForPlayer(player)
                        if (
                            minigame != null && minigame.arePlayersOnSameTeam(player, otherPlayer)
                        ) {
                            continue
                        }

                        if (otherPlayer.location.distance(bat.location) > hitboxRadius) {
                            continue
                        }

                        BrawlDamageEvent(
                                otherPlayer,
                                Damager.DamagerLivingEntity(player),
                                batDamage,
                                knockbackMultiplier = knockbackMultiplier,
                                damageType = BrawlDamageType.Projectile,
                            )
                            .callEvent()

                        bat.world.playSound(bat.location, Sound.ENTITY_BAT_HURT, 1f, 1f)

                        Particle.SMOKE.builder()
                            .location(bat.location)
                            .offset(0.0, 0.0, 0.0)
                            .count(3)
                            .extra(0.0)
                            .receivers(96, true)
                            .spawn()

                        bat.remove()
                        lastBatHitTime = System.currentTimeMillis()
                    }
                }

                if (bats.isNotEmpty() && bats.first().isLeashed) {
                    batVector.multiply(1.0 / batCount)
                    val batLocation = batVector.toLocation(player.world)
                    val difference =
                        batLocation.toVector().subtract(player.location.toVector()).normalize()
                    player.setVelocity(difference, pullStrength, false, 0.0, 0.0, 10.0, false)
                }
            }

        batTask?.let { runnables.add(it) }
    }

    override fun teardown() {
        clearBats()
        super.teardown()
    }

    private fun leashBats() {
        for (bat in bats) {
            bat.setLeashHolder(player)
        }
    }

    private fun unleashBats() {
        for (bat in bats) {
            bat.setLeashHolder(null)
        }
    }

    private fun clearBats() {
        batTask?.cancel()
        batTask = null

        unleashBats()

        for (bat in bats) {
            if (bat.isValid) {
                Particle.SMOKE.builder()
                    .location(bat.location)
                    .offset(0.0, 0.0, 0.0)
                    .count(3)
                    .extra(0.0)
                    .receivers(96, true)
                    .spawn()
            }
            bat.remove()
        }

        bats.clear()
    }
}
