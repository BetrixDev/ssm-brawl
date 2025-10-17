package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.disguises.SlimeDisguise
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageEvent
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageType
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.extensions.disguise
import dev.betrix.superSmashMobsBrawl.extensions.isOnBlock
import dev.betrix.superSmashMobsBrawl.extensions.setVelocity
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

class SlimeSlamAbility(player: Player) : BrawlAbility("slime_slam", player) {
    private val damageSize3: Double = metadata.double("damageSize3") ?: 7.0
    private val damageSize2: Double = metadata.double("damageSize2") ?: 5.0
    private val damageSize1: Double = metadata.double("damageSize1") ?: 3.0
    private val hitboxRadius: Double = metadata.double("hitboxRadius") ?: 2.0
    private val velocityStrength: Double =
        metadata.double("velocityStrength") ?: 1.2
    private val velocityYAdd: Double = metadata.double("velocityYAdd") ?: 0.2
    private val velocityYMax: Double = metadata.double("velocityYMax") ?: 1.2
    private val recoilDamageMultiplier: Double =
        metadata.double("recoilDamageMultiplier") ?: 0.25
    private val recoilKnockbackMultiplier: Double =
        metadata.double("recoilKnockbackMultiplier") ?: 2.0
    private val knockbackMultiplier: Double =
        metadata.double("knockbackMultiplier") ?: 2.0
    private val minimumLandingTimeMs: Long =
        metadata.long("minimumLandingTimeMs") ?: 1000L

    private var slamTask: TwilightRunnable? = null

    override fun activate() {
        super.activate()

        // Launch player forward in the direction they're facing
        player.setVelocity(
            velocity = player.location.direction,
            strength = velocityStrength,
            ySet = false,
            yBase = 0.0,
            yAdd = velocityYAdd,
            yMax = velocityYMax,
            groundBoost = true,
        )

        // Start monitoring for hits or landing
        startSlamTask()
    }

    override fun teardown() {
        slamTask?.cancel()
        slamTask = null
        super.teardown()
    }

    private fun startSlamTask() {
        // Cancel any existing task
        slamTask?.cancel()

        val activationTime = elapsedSinceLastActivation

        // Run every tick to check for hits or landing
        slamTask =
            repeatingTask(0, 0) {
                // Check if player is still valid
                if (!player.isValid || player.isDead) {
                    cancel()
                    slamTask = null
                    return@repeatingTask
                }

                // Check for nearby entities to hit
                val hitTarget = findNearestHitTarget()
                if (hitTarget != null) {
                    performSlam(hitTarget)
                    cancel()
                    slamTask = null
                    return@repeatingTask
                }

                // Check if player landed on ground (after minimum time)
                val timeElapsed = System.currentTimeMillis() - activationTime
                if (
                    timeElapsed >= minimumLandingTimeMs && player.isOnBlock()
                ) {
                    cancel()
                    slamTask = null
                    return@repeatingTask
                }
            }
    }

    private fun findNearestHitTarget(): LivingEntity? {
        return player.location
            .getNearbyEntities(
                hitboxRadius,
                hitboxRadius,
                hitboxRadius,
            )
            .filterIsInstance<LivingEntity>()
            .filter { it != player }
            .filter { canDamageEntity(it) }
            .minByOrNull { it.location.distance(player.location) }
    }

    private fun canDamageEntity(entity: LivingEntity): Boolean {
        // Always allow damaging non-players
        if (entity !is Player) {
            return true
        }

        // Check if players are on the same team
        val minigame = minigameService.getMinigameForPlayer(player)
        if (minigame == null) {
            return true
        }

        return !minigame.arePlayersOnSameTeam(player, entity)
    }

    private fun performSlam(target: LivingEntity) {
        val abilityDamage = getAbilityDamage()

        val recoilDamage = abilityDamage * recoilDamageMultiplier
        BrawlDamageEvent(
                player,
                Damager.DamagerLivingEntity(target),
                recoilDamage,
                recoilKnockbackMultiplier,
                BrawlDamageType.MeleeAttack,
            )
            .callEvent()

        BrawlDamageEvent(
                target,
                Damager.DamagerLivingEntity(player),
            abilityDamage,
                knockbackMultiplier,
                BrawlDamageType.MeleeAttack,
            )
            .callEvent()

        player.sendMessage(
            lang.t("messages.abilities.slimeSlam.hit") {
                "abilityId" to id
                "target" to (target as? Player)?.name.orEmpty()
            }
        )

        if (target is Player) {
            target.sendMessage(
                lang.t("messages.abilities.slimeSlam.hitBy") {
                    "abilityId" to id
                    "attacker" to player.name
                }
            )
        }
    }

    private fun getAbilityDamage(): Double {
        val playerDisguise = player.disguise

        return if (playerDisguise is SlimeDisguise) {
            when (playerDisguise.getSize()) {
                3 -> damageSize3
                2 -> damageSize2
                else -> damageSize1
            }
        } else {
            damageSize1
        }
    }
}
