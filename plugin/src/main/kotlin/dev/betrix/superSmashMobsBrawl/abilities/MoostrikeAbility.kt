package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.events.BrawlDamageEvent
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageType
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.extensions.setVelocity
import dev.betrix.superSmashMobsBrawl.services.MinigameService
import dev.betrix.superSmashMobsBrawl.utils.isOnGround
import gg.flyte.twilight.extension.addY
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.GameMode
import org.bukkit.OfflinePlayer
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MoostrikeAbility(player: Player) : BrawlAbility("moostrike", player), KoinComponent {
    private val grabRadius = metadata.double("grabRadius") ?: 3.0
    private val launchStrength = metadata.double("launchStrength") ?: 1.2
    private val maxGrabDurationMs = metadata.long("maxGrabDurationMs") ?: 2500
    private val slamDamage = metadata.double("slamDamage") ?: 6.0
    private val slamKnockbackMultiplier = metadata.double("slamKnockbackMultiplier") ?: 2.0

    private var grabbedEntity: LivingEntity? = null
    private var grabTask: TwilightRunnable? = null
    private var hasSlammed = false

    override fun activate() {
        super.activate()

        val target = findTargetInRange() ?: return

        grabbedEntity = target
        hasSlammed = false

        player.setVelocity(
            velocity = Vector(0.0, 1.0, 0.0),
            strength = launchStrength,
            ySet = true,
            yBase = launchStrength,
            yAdd = 0.0,
            yMax = 10.0,
            groundBoost = false,
        )

        target.setVelocity(
            velocity = Vector(0.0, 1.0, 0.0),
            strength = launchStrength,
            ySet = true,
            yBase = launchStrength,
            yAdd = 0.0,
            yMax = 10.0,
            groundBoost = false,
        )

        player.world.playSound(
            player.location,
            Sound.ENTITY_IRON_GOLEM_ATTACK,
            1.0f,
            1.2f,
        )

        Particle.SWEEP_ATTACK.builder()
            .location(target.location.addY(1.0))
            .count(3)
            .offset(0.5, 0.5, 0.5)
            .extra(0.1)
            .receivers(96, true)
            .spawn()

        grabTask?.cancel()
        grabTask =
            repeatingTask(1) {
                if (elapsedSinceLastActivation >= maxGrabDurationMs) {
                    releaseGrab(false)
                    cancel()
                    return@repeatingTask
                }

                val currentTarget = grabbedEntity
                if (currentTarget == null || !currentTarget.isValid || currentTarget.isDead) {
                    releaseGrab(false)
                    cancel()
                    return@repeatingTask
                }

                if (!player.isValid || player.isDead || player.gameMode == GameMode.SPECTATOR) {
                    releaseGrab(false)
                    cancel()
                    return@repeatingTask
                }

                val targetPlayer = currentTarget as? Player
                if (targetPlayer != null && targetPlayer.gameMode == GameMode.SPECTATOR) {
                    releaseGrab(false)
                    cancel()
                    return@repeatingTask
                }

                val offsetLocation = player.location.clone().add(0.0, -0.5, 0.0)
                currentTarget.teleport(offsetLocation)

                Particle.CLOUD.builder()
                    .location(currentTarget.location.addY(0.5))
                    .count(2)
                    .offset(0.3, 0.3, 0.3)
                    .extra(0.0)
                    .receivers(96, true)
                    .spawn()

                if (isOnGround(player) && !hasSlammed) {
                    performSlam()
                    cancel()
                }
            }

        grabTask?.let { runnables.add(it) }
    }

    override fun teardown() {
        releaseGrab(false)
        grabTask?.cancel()
        grabTask = null
        super.teardown()
    }

    private fun findTargetInRange(): LivingEntity? {
        return player.location
            .getNearbyEntities(grabRadius, grabRadius, grabRadius)
            .filterIsInstance<LivingEntity>()
            .filter { entity ->
                if (entity == player || entity !is OfflinePlayer) {
                    return@filter false
                }

                if (entity is Player && entity.gameMode != GameMode.SURVIVAL) {
                    return@filter false
                }

                val minigame = minigameService.getMinigameForPlayer(player)

                return@filter minigame?.arePlayersOnSameTeam(player, entity) != true
            }
            .minByOrNull { it.location.distance(player.location) }
    }

    private fun performSlam() {
        hasSlammed = true

        val target = grabbedEntity ?: return

        player.world.playSound(
            player.location,
            Sound.ENTITY_IRON_GOLEM_DAMAGE,
            1.5f,
            0.8f,
        )

        player.world.playSound(
            player.location,
            Sound.ENTITY_GENERIC_EXPLODE,
            0.5f,
            1.2f,
        )

        Particle.EXPLOSION.builder()
            .location(target.location.addY(0.5))
            .count(1)
            .receivers(96, true)
            .spawn()

        Particle.CLOUD.builder()
            .location(target.location.addY(0.5))
            .count(30)
            .offset(1.0, 0.5, 1.0)
            .extra(0.2)
            .receivers(96, true)
            .spawn()

        BrawlDamageEvent(
                target,
                Damager.DamagerLivingEntity(player),
                slamDamage,
                slamKnockbackMultiplier,
                BrawlDamageType.Slam,
            )
            .callEvent()

        releaseGrab(true)
    }

    private fun releaseGrab(wasSlammed: Boolean) {
        if (!wasSlammed) {
            grabbedEntity?.velocity = Vector(0, 0, 0)
        }
        grabbedEntity = null
        grabTask?.cancel()
        grabTask = null
    }
}

