package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.events.BrawlDamageEvent
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageType
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.extensions.isOnBlock
import dev.betrix.superSmashMobsBrawl.extensions.setVelocity
import dev.betrix.superSmashMobsBrawl.passives.CorruptedArrowPassive
import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class DeathsGraspAbility(player: Player) : BrawlAbility("deaths_grasp", player) {
    companion object {
        private val weaknessEndTimes = ConcurrentHashMap<UUID, Long>()

        fun isWeakened(player: Player): Boolean {
            val endTime = weaknessEndTimes[player.uniqueId] ?: return false
            return System.currentTimeMillis() < endTime
        }

        fun applyWeakness(player: Player, durationMs: Long) {
            weaknessEndTimes[player.uniqueId] = System.currentTimeMillis() + durationMs
        }

        fun removeWeakness(player: Player) {
            weaknessEndTimes.remove(player.uniqueId)
        }
    }

    private var leapTask: TwilightRunnable? = null
    private var leapStartTime: Long = 0

    private val damage = metadata.double("damage") ?: 2.0
    private val leapDurationMs = metadata.long("leapDurationMs") ?: 1000L
    private val weaknessDurationMs = metadata.long("weaknessDurationMs") ?: 1000L
    private val leapVelocity = metadata.double("leapVelocity") ?: 1.4
    private val leapYAdd = metadata.double("leapYAdd") ?: 0.2
    private val leapYMax = metadata.double("leapYMax") ?: 1.2
    private val throwbackVelocity = metadata.double("throwbackVelocity") ?: 1.6
    private val throwbackYAdd = metadata.double("throwbackYAdd") ?: 1.2
    private val throwbackYMax = metadata.double("throwbackYMax") ?: 1.8
    private val hitboxRadius = metadata.double("hitboxRadius") ?: 2.0
    private val rechargeTime = metadata.double("rechargeTime") ?: 2.0
    private val arrowDamageMultiplier = metadata.double("arrowDamageMultiplier") ?: 1.5
    private val arrowEnergyMultiplier = metadata.double("arrowEnergyMultiplier") ?: 1.5

    override fun setup() {
        super.setup()

        listeners.add(
            event<BrawlDamageEvent> {
                handleArrowDamageMultiplier(this)
            }
        )
    }

    override fun activate() {
        super.activate()
        startLeap()
    }

    override fun teardown() {
        leapTask?.cancel()
        leapTask = null
        super.teardown()
    }

    private fun startLeap() {
        player.setVelocity(
            player.location.direction,
            leapVelocity,
            false,
            0.0,
            leapYAdd,
            leapYMax,
            true,
        )
        player.world.playSound(player.location, Sound.ENTITY_ZOMBIE_HURT, 1f, 1.4f)

        leapStartTime = System.currentTimeMillis()

        leapTask?.cancel()
        leapTask =
            repeatingTask(0, 1) {
                if (!player.isOnline || !player.isValid) {
                    cancel()
                    return@repeatingTask
                }

                val elapsedTime = System.currentTimeMillis() - leapStartTime

                if (player.isOnBlock() && elapsedTime >= leapDurationMs) {
                    cancel()
                    return@repeatingTask
                }

                val hitTarget = findNearestHitTarget()
                if (hitTarget != null) {
                    handleCollision(hitTarget)
                    cancel()
                }
            }

        runnables.add(leapTask!!)
    }

    private fun findNearestHitTarget(): Player? {
        val minigame = minigameService.getMinigameForPlayer(player) ?: return null

        return player.location
            .getNearbyEntities(hitboxRadius, hitboxRadius, hitboxRadius)
            .filterIsInstance<Player>()
            .filter { it != player }
            .filter { !minigame.arePlayersOnSameTeam(player, it) }
            .filter { it.location.distance(player.location) < hitboxRadius }
            .minByOrNull { it.location.distance(player.location) }
    }

    private fun handleCollision(target: Player) {
        BrawlDamageEvent(
                target,
                Damager.DamagerLivingEntity(player),
                damage,
                0.0,
                BrawlDamageType.MeleeAttack,
            )
            .callEvent()

        val player2d = player.location.clone()
        player2d.y = 0.0
        val target2d = target.location.clone()
        target2d.y = 0.0
        val trajectory2d =
            player2d.toVector().subtract(target2d.toVector()).normalize().multiply(-1)

        target.setVelocity(
            trajectory2d,
            throwbackVelocity,
            false,
            0.0,
            throwbackYAdd,
            throwbackYMax,
            true,
        )

        player.velocity = Vector(0.0, 0.0, 0.0)

        player.world.playSound(player.location, Sound.ENTITY_ZOMBIE_HURT, 1f, 0.7f)

        applyWeakness(target, weaknessDurationMs)

        player.sendMessage(
            lang.t("messages.abilities.deathsGrasp.hit") {
                "abilityId" to id
                "target" to target.name
            }
        )

        target.sendMessage(
            lang.t("messages.abilities.deathsGrasp.hitBy") {
                "abilityId" to id
                "attacker" to player.name
            }
        )

        setCooldown(System.currentTimeMillis() - (abilityData.cooldown * 1000L).toLong() +
            (rechargeTime * 1000L).toLong())
    }

    private fun handleArrowDamageMultiplier(event: BrawlDamageEvent) {
        if (event.damager !is Damager.DamagerLivingEntity) return
        val damager = (event.damager).livingEntity
        if (damager != player) return
        if (event.damageType != BrawlDamageType.Projectile) return
        if (event.victim !is Player) return

        val victim = event.victim
        if (!isWeakened(victim)) return

        val baseDamage = event.damage
        event.damage *= arrowDamageMultiplier

        // Grant bonus energy for empowered shots (1.5x energy on damage dealt)
        val damageDealt = event.damage
        CorruptedArrowPassive.addEnergy(player, damageDealt * 1.5, arrowEnergyMultiplier, energyManager)

        Particle.DUST.builder()
            .location(victim.location.add(0.0, 1.0, 0.0))
            .offset(0.5, 0.5, 0.5)
            .count(20)
            .extra(1.0)
            .receivers(96, true)
            .spawn()

        Particle.EXPLOSION.builder()
            .location(victim.location.add(0.0, 1.0, 0.0))
            .count(1)
            .receivers(96, true)
            .spawn()

        player.world.playSound(player.location, Sound.ENTITY_ZOMBIE_HURT, 1f, 2f)
    }
}
