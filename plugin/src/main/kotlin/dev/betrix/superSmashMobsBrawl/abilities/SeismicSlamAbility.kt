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
import kotlin.random.Random
import org.bukkit.Effect
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class SeismicSlamAbility(player: Player) : BrawlAbility("seismic_slam", player) {

    private val launchStrength: Double = metadata.double("launchStrength") ?: 1.0
    private val launchYAdd: Double = metadata.double("launchYAdd") ?: 1.0
    private val launchYMax: Double = metadata.double("launchYMax") ?: 1.0
    private val launchGroundBoost: Boolean = metadata.boolean("launchGroundBoost") ?: true
    private val landingCheckDelayTicks: Long = (metadata.long("landingCheckDelayTicks") ?: 20L)
    private val landingCheckIntervalTicks: Long =
        (metadata.long("landingCheckIntervalTicks") ?: 1L).coerceAtLeast(1L)
    private val baseDamage: Double = metadata.double("baseDamage") ?: 10.0
    private val damageRange: Double = metadata.double("damageRange") ?: 8.0
    private val knockbackMultiplier: Double = metadata.double("knockbackMultiplier") ?: 2.4
    private val blockEffectRadius: Int = (metadata.int("blockEffectRadius") ?: 4).coerceAtLeast(0)
    private val blockEffectChance: Double = metadata.double("blockEffectChance") ?: 0.1

    private var landingCheckTask: TwilightRunnable? = null

    override fun activate() {
        super.activate()

        val launchDirection: Vector = player.location.direction.clone().apply { y = abs(y) }

        player.setVelocity(
            velocity = launchDirection,
            strength = launchStrength,
            ySet = false,
            yBase = 0.0,
            yAdd = launchYAdd,
            yMax = launchYMax,
            groundBoost = launchGroundBoost,
        )

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
                    doSlam()
                }
            }

        landingCheckTask?.let { runnables.add(it) }
    }

    override fun teardown() {
        landingCheckTask?.cancel()
        landingCheckTask = null
        super.teardown()
    }

    private fun doSlam() {
        val ownerLocation = player.location

        ownerLocation
            .getNearbyEntities(damageRange,damageRange,damageRange)
            .mapNotNull { it as? LivingEntity }
            .filter { it != player }
            .forEach { target ->
                val distance = target.location.distance(ownerLocation)
                val normalizedDistance = ((damageRange - distance) / damageRange).coerceAtLeast(0.0)
                val scaledDamage = (baseDamage * normalizedDistance) + 0.5

                BrawlDamageEvent(
                        target,
                        Damager.DamagerLivingEntity(player),
                        scaledDamage,
                        knockbackMultiplier,
                        BrawlDamageType.Explosion,
                    )
                    .callEvent()
            }

        ownerLocation.world.playSound(ownerLocation, Sound.BLOCK_ANVIL_LAND, 2f, 0.2f)

        triggerBlockEffects(ownerLocation.block)
    }

    private fun triggerBlockEffects(origin: Block) {
        if (blockEffectRadius <= 0 || blockEffectChance <= 0.0) {
            return
        }

        val world = origin.world

        for (x in -blockEffectRadius..blockEffectRadius) {
            for (y in -blockEffectRadius..blockEffectRadius) {
                for (z in -blockEffectRadius..blockEffectRadius) {
                    if (Random.nextDouble() >= blockEffectChance) {
                        continue
                    }

                    val checkBlock = origin.getRelative(x, y, z)

                    if (!checkBlock.type.isSolid) {
                        continue
                    }

                    val aboveBlock = checkBlock.getRelative(BlockFace.UP)

                    if (aboveBlock.type.isSolid) {
                        continue
                    }

                    world.playEffect(checkBlock.location, Effect.STEP_SOUND, checkBlock.type)
                }
            }
        }
    }
}

