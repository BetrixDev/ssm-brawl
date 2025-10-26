package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.disguises.SheepDisguise
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageEvent
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageType
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.extensions.disguise
import dev.betrix.superSmashMobsBrawl.extensions.setVelocity
import dev.betrix.superSmashMobsBrawl.passives.DoubleJumpPassive
import dev.betrix.superSmashMobsBrawl.utils.isOnGround
import gg.flyte.twilight.extension.getNearbyEntities
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.DyeColor
import org.bukkit.Effect
import org.bukkit.Particle
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class WoolyRocketAbility(player: Player) : BrawlAbility("wooly_rocket", player) {

    private val damage = metadata.double("damage") ?: 8.0
    private val damageRadius = metadata.double("damageRadius") ?: 2.0
    private val knockbackMultiplier =
        metadata.double("knockbackMultiplier") ?: 2.5
    private val minimumVelocityTimeMs =
        metadata.long("minimumVelocityTimeMs") ?: 200L
    private val maximumVelocityTimeMs =
        metadata.long("maximumVelocityTimeMs") ?: 1200L

    private var rocketTask: TwilightRunnable? = null

    override fun activate() {
        super.activate()

        player.setVelocity(Vector(0.0, 1.0, 0.0), 1.0, false, 0.0, 0.0, 2.0, true)

        player.world.playEffect(player.location, Effect.BLAZE_SHOOT, 0)

        val kit = kitService.getKitForPlayer(player)
        if (kit != null) {
            val doubleJump = kit.getPassive("double_jump") as? DoubleJumpPassive
            if (doubleJump != null) {
                player.allowFlight = true
            }
        }

        setSheepColor(DyeColor.RED)

        startRocketTask()
    }

    private fun startRocketTask() {
        rocketTask?.cancel()

        rocketTask =
            repeatingTask(0, 0) {
                if (!player.isValid || !player.isOnline) {
                    stopRocket()
                    cancel()
                    return@repeatingTask
                }

                Particle.FLAME.builder()
                    .location(player.location)
                    .count(4)
                    .offset(0.2, 0.2, 0.2)
                    .extra(0.0)
                    .receivers(96, true)
                    .spawn()

                val timeElapsed = elapsedSinceLastActivation

                if (timeElapsed < minimumVelocityTimeMs) {
                    return@repeatingTask
                }

                player.location
                    .getNearbyEntities(damageRadius, damageRadius, damageRadius)
                    .filterIsInstance<Player>()
                    .filter { it != player }
                    .filter { canDamageEntity(it) }
                    .forEach { entity ->
                        BrawlDamageEvent(
                                entity,
                                Damager.DamagerLivingEntity(player),
                                damage,
                                knockbackMultiplier,
                                BrawlDamageType.Projectile,
                            )
                            .callEvent()

                        Particle.EXPLOSION.builder()
                            .location(entity.location)
                            .count(1)
                            .receivers(96, true)
                            .spawn()

                        Particle.LAVA.builder()
                            .location(entity.location)
                            .count(10)
                            .offset(0.2, 0.2, 0.2)
                            .extra(0.0)
                            .receivers(96, true)
                            .spawn()
                    }

                if (isOnGround(player) || timeElapsed >= maximumVelocityTimeMs) {
                    stopRocket()
                    cancel()
                    return@repeatingTask
                }
            }

        rocketTask?.let { runnables.add(it) }
    }

    private fun stopRocket() {
        rocketTask?.cancel()
        rocketTask = null
        setSheepColor(DyeColor.WHITE)
    }

    private fun canDamageEntity(entity: LivingEntity): Boolean {
        if (entity !is Player) {
            return true
        }

        val minigame = minigameService.getMinigameForPlayer(player) ?: return true

        return !minigame.arePlayersOnSameTeam(player, entity)
    }

    private fun setSheepColor(color: DyeColor) {
        (player.disguise as? SheepDisguise)?.setColor(color)
    }

    override fun teardown() {
        stopRocket()
        super.teardown()
    }
}


