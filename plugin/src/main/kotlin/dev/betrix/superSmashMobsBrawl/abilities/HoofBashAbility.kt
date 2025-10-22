package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.events.BrawlDamageEvent
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageType
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import gg.flyte.twilight.extension.getNearbyEntities
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.delay
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector

class HoofBashAbility(player: Player) : BrawlAbility("hoof_bash", player) {

    private val damage: Double = metadata.double("damage") ?: 6.5
    private val knockbackMultiplier: Double =
        metadata.double("knockbackMultiplier") ?: 4.0
    private val range: Double = metadata.double("range") ?: 2.5
    private val slowDurationTicks: Int = metadata.int("slowDurationTicks") ?: 16
    private val animationDurationMs: Long = metadata.long("animationDurationMs") ?: 1000L

    private var animationTask: TwilightRunnable? = null

    override fun activate() {
        super.activate()

        val location = player.location.clone()
        location.add(player.location.direction.setY(0).normalize().multiply(1.5))
        location.add(0.0, 0.8, 0.0)

        // Apply damage and knockback to nearby entities
        location
            .getNearbyEntities(range, range, range)
            .mapNotNull { it as? LivingEntity }
            .filter { it != player }
            .filter { canDamageEntity(it) }
            .forEach { target ->
                BrawlDamageEvent(
                        target,
                        Damager.DamagerLivingEntity(player),
                        damage,
                        knockbackMultiplier,
                        BrawlDamageType.MeleeAttack,
                    )
                    .callEvent()

                player.world.playSound(
                    player.location,
                    Sound.ENTITY_SKELETON_HURT,
                    4f,
                    0.6f,
                )

                if (target is Player) {
                    target.sendMessage(
                        lang.t("messages.abilities.hoofBash.hitBy") {
                            "abilityId" to id
                            "attacker" to player.name
                        }
                    )
                }
            }

        // Slow the player during animation
        player.removePotionEffect(PotionEffectType.SLOWNESS)
        player.addPotionEffect(
            PotionEffect(
                PotionEffectType.SLOWNESS,
                slowDurationTicks,
                3,
                false,
                false,
            )
        )
        player.velocity = Vector(0, 0, 0)

        // Spawn particles during animation
        val activationTime = System.currentTimeMillis()
        animationTask?.cancel()
        animationTask =
            delay(0) {
                val elapsed = System.currentTimeMillis() - activationTime
                if (elapsed >= animationDurationMs) {
                    cancel()
                    animationTask = null
                    return@delay
                }

                val particleLocation = player.location.clone()
                particleLocation.add(
                    player.location.direction.setY(0).normalize().multiply(1.5)
                )
                particleLocation.add(0.0, 0.8, 0.0)

                player.world.spawnParticle(
                    Particle.SMOKE,
                    particleLocation,
                    2,
                    0.3,
                    0.3,
                    0.3,
                    0.0,
                )
            }

        animationTask?.let { runnables.add(it) }
    }

    override fun teardown() {
        animationTask?.cancel()
        animationTask = null
        super.teardown()
    }

    private fun canDamageEntity(entity: LivingEntity): Boolean {
        if (entity !is Player) {
            return true
        }

        val minigame = minigameService.getMinigameForPlayer(player) ?: return true

        return !minigame.arePlayersOnSameTeam(player, entity)
    }
}

