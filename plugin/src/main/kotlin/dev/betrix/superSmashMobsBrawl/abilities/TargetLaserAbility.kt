package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.disguises.GuardianDisguise
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageEvent
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageType
import dev.betrix.superSmashMobsBrawl.events.BrawlDeathEvent
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.extensions.disguise
import dev.betrix.superSmashMobsBrawl.extensions.event
import dev.betrix.superSmashMobsBrawl.extensions.isOnBlock
import gg.flyte.twilight.event.event
import gg.flyte.twilight.extension.getNearbyEntities
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.Effect
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.Player

class TargetLaserAbility(player: Player) : BrawlAbility("target_laser", player) {

    private val maxRange: Double = metadata.double("maxRange") ?: 11.0
    private val damageIncrease: Double = metadata.double("damageIncrease") ?: 2.0
    private val knockbackIncreaseMultiplier: Double =
        metadata.double("knockbackIncreaseMultiplier") ?: 1.25
    private val maxTimeMs: Long = (metadata.long("maxTimeMs") ?: 8000L).coerceAtLeast(0L)
    private val breakDamagePerSecond: Double = metadata.double("breakDamagePerSecond") ?: 0.5

    private var targetPlayer: Player? = null
    private var laserTask: TwilightRunnable? = null

    override fun setup() {
        super.setup()

        listeners.add(
            event<BrawlDamageEvent> {
                val target = targetPlayer ?: return@event

                if (victim != target) {
                    return@event
                }

                val attackerLivingEntity =
                    (damager as? Damager.DamagerLivingEntity)?.livingEntity ?: return@event

                if (attackerLivingEntity != this@TargetLaserAbility.player) {
                    return@event
                }

                damage += damageIncrease
                knockbackMultiplier *= knockbackIncreaseMultiplier

                target.world.playEffect(
                    target.location.add(0.0, 0.5, 0.0),
                    Effect.STEP_SOUND,
                    Material.REDSTONE_BLOCK,
                )
            }
        )

        listeners.add(
            event<BrawlDeathEvent> {
                val target = targetPlayer ?: return@event

                if (player != target) {
                    return@event
                }

                setCooldown(0)
                stopLaser()
            }
        )
    }

    override fun canActivate(sendMessage: Boolean): Boolean {
        if (!super.canActivate(sendMessage)) {
            return false
        }

        if (targetPlayer != null) {
            if (sendMessage) {
                player.sendMessage(
                    lang.t("messages.abilities.targetLaser.alreadyTargeting") { "abilityId" to id }
                )
            }
            return false
        }

        if (!player.isOnBlock()) {
            if (sendMessage) {
                player.sendMessage(
                    lang.t("messages.abilities.targetLaser.mustBeOnGround") { "abilityId" to id }
                )
            }
            return false
        }

        return true
    }

    override fun activate() {
        val target = findNearestTarget()

        if (target == null) {
            player.sendMessage(
                lang.t("messages.abilities.targetLaser.noTargetsInRange") { "abilityId" to id }
            )
            return
        }

        super.activate()
        targetPlayer = target
        setGuardianTarget(target)

        player.sendMessage(
            lang.t("messages.abilities.targetLaser.targeted") {
                "abilityId" to id
                "target" to target.name
            }
        )
        target.sendMessage(
            lang.t("messages.abilities.targetLaser.targetedBy") {
                "abilityId" to id
                "attacker" to player.name
            }
        )

        startLaserTask()
    }

    override fun teardown() {
        stopLaser()
        super.teardown()
    }

    private fun findNearestTarget(): Player? {
        val minigame = minigameService.getMinigameForPlayer(player) ?: return null

        return player.location
            .getNearbyEntities(maxRange, maxRange, maxRange)
            .filterIsInstance<Player>()
            .filter { it != player }
            .filter { !minigame.arePlayersOnSameTeam(player, it) }
            .filter { it.location.distance(player.location) <= maxRange }
            .minByOrNull { it.location.distance(player.location) }
    }

    private fun startLaserTask() {
        laserTask?.cancel()
        val activationTime = lastUsed

        laserTask =
            repeatingTask(0, 5) {
                val target = targetPlayer
                if (target == null || !target.isValid || !target.isOnline) {
                    stopLaser()
                    cancel()
                    return@repeatingTask
                }

                Particle.ENCHANTED_HIT.builder()
                    .location(target.location.add(0.0, 0.5, 0.0))
                    .offset(0.5, 1.0, 0.5)
                    .count(10)
                    .extra(0.1)
                    .receivers(96, true)
                    .spawn()

                val timeElapsed = System.currentTimeMillis() - activationTime
                val distance = player.location.distance(target.location)

                if (distance > maxRange || timeElapsed >= maxTimeMs) {
                    val seconds = timeElapsed / 1000.0
                    val breakDamage = breakDamagePerSecond * seconds

                    player.sendMessage(
                        lang.t("messages.abilities.targetLaser.laserBroke") {
                            "abilityId" to id
                            "target" to target.name
                        }
                    )

                    BrawlDamageEvent(
                            target,
                            Damager.DamagerLivingEntity(player),
                            breakDamage,
                            0.0,
                            BrawlDamageType.Explosion,
                        )
                        .callEvent()

                    stopLaser()
                    cancel()
                }
            }

        laserTask?.let { runnables.add(it) }
    }

    private fun stopLaser() {
        laserTask?.cancel()
        laserTask = null
        setGuardianTarget(null)
        targetPlayer = null
    }

    private fun setGuardianTarget(target: Player?) {
        (player.disguise as? GuardianDisguise)?.setTarget(target)
    }
}
