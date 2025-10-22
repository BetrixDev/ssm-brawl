package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.events.BrawlDamageEvent
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageType
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.extensions.isOnBlock
import dev.betrix.superSmashMobsBrawl.extensions.setVelocity
import gg.flyte.twilight.extension.getNearbyEntities
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import kotlin.math.abs
import org.bukkit.Effect
import org.bukkit.Sound
import org.bukkit.block.BlockFace
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class GlidingGallopAbility(player: Player) : BrawlAbility("gliding_gallop", player) {

    private val launchStrength: Double = metadata.double("launchStrength") ?: 1.0
    private val launchYAdd: Double = metadata.double("launchYAdd") ?: 1.5
    private val launchYMax: Double = metadata.double("launchYMax") ?: 2.0
    private val landingCheckDelayTicks: Long =
        metadata.long("landingCheckDelayTicks") ?: 10L
    private val landingCheckIntervalTicks: Long =
        (metadata.long("landingCheckIntervalTicks") ?: 1L).coerceAtLeast(1L)
    private val impactDamage: Double = metadata.double("impactDamage") ?: 5.0
    private val impactRadius: Double = metadata.double("impactRadius") ?: 4.0
    private val impactKnockbackMultiplier: Double =
        metadata.double("impactKnockbackMultiplier") ?: 2.0
    private val impactEnemyLaunchStrength: Double =
        metadata.double("impactEnemyLaunchStrength") ?: 1.8
    private val impactEnemyLaunchYAdd: Double =
        metadata.double("impactEnemyLaunchYAdd") ?: 1.5

    private var landingCheckTask: TwilightRunnable? = null

    override fun activate() {
        super.activate()

        // Launch player upward
        val launchDirection: Vector =
            player.location.direction.clone().apply { y = abs(y) }

        player.setVelocity(
            velocity = launchDirection,
            strength = launchStrength,
            ySet = false,
            yBase = 0.0,
            yAdd = launchYAdd,
            yMax = launchYMax,
            groundBoost = true,
        )

        player.world.playSound(player.location, Sound.ENTITY_HORSE_JUMP, 1.5f, 1.0f)

        // Start checking for landing
        landingCheckTask?.cancel()
        landingCheckTask =
            repeatingTask(landingCheckDelayTicks, landingCheckIntervalTicks) {
                if (!player.isValid || player.isDead) {
                    cancel()
                    landingCheckTask = null
                    return@repeatingTask
                }

                if (player.isOnBlock()) {
                    cancel()
                    landingCheckTask = null
                    performImpact()
                }
            }

        landingCheckTask?.let { runnables.add(it) }
    }

    override fun teardown() {
        landingCheckTask?.cancel()
        landingCheckTask = null
        super.teardown()
    }

    private fun performImpact() {
        val ownerLocation = player.location

        // Launch nearby enemies into the air
        ownerLocation
            .getNearbyEntities(impactRadius, impactRadius, impactRadius)
            .mapNotNull { it as? LivingEntity }
            .filter { it != player }
            .filter { canDamageEntity(it) }
            .forEach { target ->
                val distance = target.location.distance(ownerLocation)
                val normalizedDistance =
                    ((impactRadius - distance) / impactRadius).coerceAtLeast(0.0)
                val scaledDamage = (impactDamage * normalizedDistance) + 0.5

                BrawlDamageEvent(
                        target,
                        Damager.DamagerLivingEntity(player),
                        scaledDamage,
                        impactKnockbackMultiplier,
                        BrawlDamageType.Explosion,
                    )
                    .callEvent()

                // Launch enemy upward
                val launchDirection =
                    target.location
                        .toVector()
                        .subtract(ownerLocation.toVector())
                        .normalize()
                target.setVelocity(
                    velocity = launchDirection,
                    strength = impactEnemyLaunchStrength * normalizedDistance,
                    ySet = false,
                    yBase = 0.0,
                    yAdd = impactEnemyLaunchYAdd,
                    yMax = 10.0,
                    groundBoost = false,
                )

                if (target is Player) {
                    target.sendMessage(
                        lang.t("messages.abilities.glidingGallop.hitBy") {
                            "abilityId" to id
                            "attacker" to player.name
                        }
                    )
                }
            }

        // Play impact sound and effects
        ownerLocation.world.playSound(
            ownerLocation,
            Sound.ENTITY_IRON_GOLEM_HURT,
            2f,
            0.6f,
        )
        ownerLocation.world.playSound(
            ownerLocation,
            Sound.ENTITY_HORSE_LAND,
            2f,
            1.0f,
        )

        // Spawn particle effects
        ownerLocation.world.spawnParticle(
            org.bukkit.Particle.EXPLOSION,
            ownerLocation,
            3,
            1.0,
            0.5,
            1.0,
            0.0,
        )

        // Trigger block effects
        val originBlock = ownerLocation.block
        for (x in -2..2) {
            for (z in -2..2) {
                val checkBlock = originBlock.getRelative(x, 0, z)

                if (!checkBlock.type.isSolid) {
                    continue
                }

                val aboveBlock = checkBlock.getRelative(BlockFace.UP)

                if (aboveBlock.type.isSolid) {
                    continue
                }

                if (Math.random() < 0.3) {
                    ownerLocation.world.playEffect(
                        checkBlock.location,
                        Effect.STEP_SOUND,
                        checkBlock.type,
                    )
                }
            }
        }
    }

    private fun canDamageEntity(entity: LivingEntity): Boolean {
        if (entity !is Player) {
            return true
        }

        val minigame = minigameService.getMinigameForPlayer(player) ?: return true

        return !minigame.arePlayersOnSameTeam(player, entity)
    }
}

