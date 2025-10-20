package dev.betrix.superSmashMobsBrawl.passives

import dev.betrix.superSmashMobsBrawl.events.BrawlDamageEvent
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageType
import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import kotlin.math.min
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.persistence.PersistentDataType

class CorruptedArrowPassive(player: Player) : BrawlPassive("corrupted_arrow", player) {

    private val maxCharge: Int
        get() = metadata.int("maxCharge") ?: 7

    private val damagePerCharge: Double
        get() = metadata.double("damagePerCharge") ?: 0.9

    private val chargeDelayTicks: Long
        get() = metadata.long("chargeDelayTicks") ?: 0L

    private val chargeRateTicks: Long
        get() = metadata.long("chargeRateTicks") ?: 6L

    private var charge = 0
    private var chargeRunnable: TwilightRunnable? = null
    private val firedArrows = mutableListOf<Arrow>()
    private val damageBoostKey = NamespacedKey(plugin, "corrupted_arrow_damage_boost")

    override fun setup() {
        setupBowChargingListener()
        setupItemHeldListener()
        setupBowFireListener()
        setupDamageListener()
        setupParticleTask()
        super.setup()
    }

    private fun setupBowChargingListener() {
        listeners.add(
            event<PlayerInteractEvent> {
                if (
                    player != this@CorruptedArrowPassive.player ||
                        player.inventory.itemInMainHand.type != Material.BOW ||
                        !(player.inventory.contains(Material.ARROW) ||
                            player.inventory.contains(Material.TIPPED_ARROW) ||
                            player.inventory.contains(Material.SPECTRAL_ARROW)) ||
                        (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK)
                ) {
                    return@event
                }

                finishCharging()

                val newChargeRunnable =
                    repeatingTask(chargeDelayTicks, chargeRateTicks) {
                        if (charge < maxCharge) {
                            incrementCharge()
                        }
                    }

                chargeRunnable = newChargeRunnable
                runnables.add(newChargeRunnable)
            }
        )
    }

    private fun setupItemHeldListener() {
        listeners.add(
            event<PlayerItemHeldEvent> {
                if (player != this@CorruptedArrowPassive.player) {
                    return@event
                }

                finishCharging()
            }
        )
    }

    private fun setupBowFireListener() {
        listeners.add(
            event<EntityShootBowEvent> {
                if (projectile !is Arrow) {
                    return@event
                }

                val arrow = projectile as Arrow
                val source = arrow.shooter

                if (source != player || entity != player) {
                    return@event
                }

                onBowFired(arrow)
                finishCharging()
            }
        )
    }

    private fun setupDamageListener() {
        // Listen to vanilla arrow damage and convert to BrawlDamageEvent
        // Note: We process even if cancelled, since CombatManager cancels arrow damage
        listeners.add(
            event<org.bukkit.event.entity.EntityDamageByEntityEvent> {
                val arrow = damager as? Arrow ?: return@event
                val victim = entity as? org.bukkit.entity.LivingEntity ?: return@event

                // Check if this arrow has corrupted charge
                val arrowCharge =
                    arrow.persistentDataContainer.get(damageBoostKey, PersistentDataType.INTEGER)
                        ?: return@event

                if (arrowCharge <= 0) return@event

                // Check if arrow was shot by this player
                if (arrow.shooter != player) return@event

                // Cancel vanilla damage and apply our custom damage
                isCancelled = true

                val baseDamage = damage
                val totalDamage = baseDamage + (arrowCharge * damagePerCharge)

                BrawlDamageEvent(
                        victim,
                        dev.betrix.superSmashMobsBrawl.events.Damager.DamagerLivingEntity(player),
                        totalDamage,
                        damageType = BrawlDamageType.Projectile,
                    )
                    .callEvent()

                // Remove arrow from tracking
                firedArrows.remove(arrow)
            }
        )
    }

    private fun setupParticleTask() {
        runnables.add(
            repeatingTask(1) {
                if (!player.isOnline || player.isDead) {
                    return@repeatingTask
                }

                // Clean up dead/grounded arrows and show particles
                firedArrows.removeAll { arrow ->
                    if (arrow.isDead || !arrow.isValid || arrow.isOnGround || arrow.ticksLived > 120) {
                        true
                    } else {
                        // Show particle effect on this corrupted arrow
                        Particle.DUST
                            .builder()
                            .location(arrow.location)
                            .count(1)
                            .offset(0.0, 0.0, 0.0)
                            .extra(0.0)
                            .data(Particle.DustOptions(org.bukkit.Color.RED, 1.0f))
                            .receivers(96, true)
                            .spawn()
                        false
                    }
                }
            }
        )
    }

    private fun onBowFired(arrow: Arrow) {
        if (charge > 0) {
            // Store charge on arrow using persistent data
            arrow.persistentDataContainer.set(damageBoostKey, PersistentDataType.INTEGER, charge)
            firedArrows.add(arrow)
        }
    }

    private fun incrementCharge() {
        charge++
        player.exp = min(0.9999F, charge.toFloat() / maxCharge.toFloat())
        player.playSound(player.eyeLocation, Sound.BLOCK_DISPENSER_FAIL, 0.5f, 1 + 0.1f * charge)
    }

    private fun finishCharging() {
        chargeRunnable?.let {
            runnables.remove(it)
            it.cancel()
        }
        chargeRunnable = null
        player.exp = 0f
        charge = 0
    }

    override fun teardown() {
        finishCharging()
        firedArrows.clear()
        super.teardown()
    }
}
