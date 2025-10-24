package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.disguises.SheepDisguise
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageEvent
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageType
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.extensions.disguise
import gg.flyte.twilight.extension.getNearbyEntities
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.*
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector
import kotlin.math.min

class StaticLaserAbility(player: Player) :
    BrawlAbility("static_laser", player) {

    private var chargingTask: TwilightRunnable? = null
    private var isCharging = false

    private val damage = metadata.double("damage") ?: 7.0
    private val hitboxRadius = metadata.double("hitboxRadius") ?: 1.5
    private val damageRadius = metadata.double("damageRadius") ?: 2.5
    private val knockbackMultiplier = metadata.double("knockbackMultiplier") ?: 3.0
    private val range = metadata.double("range") ?: 40.0
    private val maxCharge = metadata.float("maxCharge") ?: 0.99f
    private val chargePerTick = metadata.float("chargePerTick") ?: 0.035f

    override fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.hand != null && event.hand != EquipmentSlot.HAND) return

        if (!isCorrectActionForUsage(event.action)) {
            return
        }

        val item = event.item ?: return

        if (!isCorrectItemForAbility(item)) {
            return
        }

        event.isCancelled = true

        if (isCharging) {
            return
        }

        if (!canActivate()) {
            return
        }

        startCharging()
    }

    private fun startCharging() {
        isCharging = true
        activate()

        chargingTask?.cancel()
        chargingTask =
            repeatingTask(0, 0) {
                if (!player.isValid || !player.isOnline) {
                    stopCharging()
                    cancel()
                    return@repeatingTask
                }

                if (!player.isBlocking) {
                    stopCharging()
                    fireLaser()
                    cancel()
                    return@repeatingTask
                }

                player.exp = min(maxCharge, player.exp + chargePerTick)

                player.world.playSound(
                    player.location,
                    Sound.BLOCK_FIRE_EXTINGUISH,
                    0.25f + player.exp,
                    0.75f + player.exp,
                )

                setSheepColor(
                    if (Math.random() > 0.5) DyeColor.YELLOW else DyeColor.BLACK
                )

                if (player.exp >= maxCharge) {
                    stopCharging()
                    fireLaser()
                    cancel()
                    return@repeatingTask
                }
            }

        chargingTask?.let { runnables.add(it) }
    }

    private fun stopCharging() {
        isCharging = false
        chargingTask?.cancel()
        chargingTask = null
    }

    private fun fireLaser() {
        if (player.exp <= 0.2) {
            setSheepColor(DyeColor.WHITE)
            player.exp = 0f
            return
        }

        val chargeLevel = player.exp
        val effectiveRange = range * chargeLevel

        val start = player.eyeLocation
        val direction = start.direction

        val result =
            player.world.rayTraceBlocks(
                start,
                direction,
                effectiveRange,
                FluidCollisionMode.NEVER,
                true,
            )

        val endLocation =
            result?.hitPosition?.toLocation(player.world)
                ?: start.clone().add(direction.clone().multiply(effectiveRange))

        drawLaserBeam(start, endLocation)

        val targetPlayer = findTargetInBeam(start, direction, effectiveRange)

        if (targetPlayer != null) {
            BrawlDamageEvent(
                    targetPlayer,
                    Damager.DamagerLivingEntity(player),
                    damage * chargeLevel,
                    knockbackMultiplier,
                    BrawlDamageType.Projectile,
                )
                .callEvent()
        }

        val groundHitLocation = endLocation.clone().subtract(0.0, 1.0, 0.0)
        groundHitLocation
            .getNearbyEntities(damageRadius, damageRadius, damageRadius)
            .filterIsInstance<LivingEntity>()
            .filter { it != player }
            .filter { canDamageEntity(it) }
            .forEach { entity ->
                if (entity != targetPlayer) {
                    BrawlDamageEvent(
                            entity,
                            Damager.DamagerLivingEntity(player),
                            damage * chargeLevel,
                            knockbackMultiplier,
                            BrawlDamageType.Projectile,
                        )
                        .callEvent()
                }
            }

        Particle.EXPLOSION.builder()
            .location(endLocation)
            .count(1)
            .receivers(96, true)
            .spawn()

        player.world.playSound(
            player.eyeLocation,
            Sound.ENTITY_ZOMBIE_VILLAGER_CURE,
            0.5f + chargeLevel,
            1.75f - chargeLevel,
        )
        player.world.playSound(
            player.location,
            Sound.ENTITY_SHEEP_AMBIENT,
            2f,
            1.5f,
        )

        setSheepColor(DyeColor.WHITE)
        player.exp = 0f
    }

    private fun drawLaserBeam(start: Location, end: Location) {
        val distance = start.distance(end)
        val step = 0.2
        val steps = (distance / step).toInt()

        val direction = end.toVector().subtract(start.toVector()).normalize()

        for (i in 0..steps) {
            val point =
                start.clone().add(direction.clone().multiply(i * step))
            Particle.FIREWORK.builder()
                .location(point)
                .count(1)
                .offset(0.0, 0.0, 0.0)
                .extra(0.0)
                .receivers(96, true)
                .spawn()
        }
    }

    private fun findTargetInBeam(
        start: Location,
        direction: Vector,
        range: Double
    ): Player? {
        val step = 0.2
        val steps = (range / step).toInt()

        for (i in 0..steps) {
            val checkLocation = start.clone().add(direction.clone().multiply(i * step))
            val nearby =
                checkLocation
                    .getNearbyEntities(hitboxRadius, hitboxRadius, hitboxRadius)
                    .filterIsInstance<Player>()
                    .filter { it != player }
                    .filter { canDamageEntity(it) }

            if (nearby.isNotEmpty()) {
                return nearby.minByOrNull { it.location.distance(checkLocation) }
            }
        }

        return null
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
        stopCharging()
        setSheepColor(DyeColor.WHITE)
        super.teardown()
    }
}

