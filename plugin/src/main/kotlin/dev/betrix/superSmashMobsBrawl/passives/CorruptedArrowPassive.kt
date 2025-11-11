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
    companion object {
        private val lastDamageTime = mutableMapOf<java.util.UUID, Long>()

        fun getEnergy(player: Player, energyManager: dev.betrix.superSmashMobsBrawl.kits.KitEnergyManager?): Double {
            return energyManager?.getCurrentEnergy() ?: 0.0
        }

        fun addEnergy(
            player: Player,
            amount: Double,
            multiplier: Double = 1.0,
            energyManager: dev.betrix.superSmashMobsBrawl.kits.KitEnergyManager?,
        ) {
            energyManager?.addEnergy(amount * multiplier)
            lastDamageTime[player.uniqueId] = System.currentTimeMillis()
        }

        fun clearEnergy(player: Player) {
            lastDamageTime.remove(player.uniqueId)
        }
    }

    private val maxCharge: Int
        get() = metadata.int("maxCharge") ?: 7

    private val damagePerCharge: Double
        get() = metadata.double("damagePerCharge") ?: 0.9

    private val chargeDelayTicks: Long
        get() = metadata.long("chargeDelayTicks") ?: 0L

    private val chargeRateTicks: Long
        get() = metadata.long("chargeRateTicks") ?: 6L

    private val minDamage: Double
        get() = metadata.double("minDamage") ?: 5.0

    private val maxDamage: Double
        get() = metadata.double("maxDamage") ?: 12.0

    private val energyPerDamage: Double
        get() = metadata.double("energyPerDamage") ?: 1.5

    private val energyDecayDelayMs: Long
        get() = metadata.long("energyDecayDelayMs") ?: 3000L

    private val energyDecayRate: Double
        get() = metadata.double("energyDecayRate") ?: 10.0

    private val baseBowDamageCap: Double
        get() = metadata.double("baseBowDamageCap") ?: 5.0

    private var charge = 0
    private var chargeRunnable: TwilightRunnable? = null
    private val firedArrows = mutableListOf<Arrow>()
    private val damageBoostKey = NamespacedKey(plugin, "corrupted_arrow_damage_boost")

    override fun setup() {
        setupBowChargingListener()
        setupItemHeldListener()
        setupBowFireListener()
        setupDamageListener()
        setupDamageGainListener()
        setupEnergyDecayTask()
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

                // Check if arrow was shot by this player
                if (arrow.shooter != player) return@event

                // Cancel vanilla damage and apply our custom damage
                isCancelled = true

                // Check if this arrow has corrupted charge
                val arrowCharge =
                    arrow.persistentDataContainer.get(damageBoostKey, PersistentDataType.INTEGER)
                        ?: 0

                val calculatedDamage = if (arrowCharge > 0) {
                    // Corrupted arrow: scale damage based on energy (5-12)
                    val maxEnergy = energyManager?.getMaxEnergy() ?: 100.0
                    val energy = getEnergy(player, energyManager)
                    val energyRatio = energy / maxEnergy
                    minDamage + (maxDamage - minDamage) * energyRatio
                } else {
                    // Non-corrupted arrow: cap at base damage
                    damage.coerceAtMost(baseBowDamageCap)
                }

                BrawlDamageEvent(
                        victim,
                        dev.betrix.superSmashMobsBrawl.events.Damager.DamagerLivingEntity(player),
                        calculatedDamage,
                        damageType = BrawlDamageType.Projectile,
                    )
                    .callEvent()

                // Remove arrow from tracking
                firedArrows.remove(arrow)
            }
        )
    }

    private fun setupDamageGainListener() {
        listeners.add(
            event<BrawlDamageEvent> {
                // Only track damage dealt by this player
                if (damager !is dev.betrix.superSmashMobsBrawl.events.Damager.DamagerLivingEntity) return@event
                val source = (damager as dev.betrix.superSmashMobsBrawl.events.Damager.DamagerLivingEntity).livingEntity
                if (source != player) return@event

                // Gain energy based on damage dealt
                addEnergy(player, damage * energyPerDamage, 1.0, energyManager)
            }
        )
    }

    private fun setupEnergyDecayTask() {
        runnables.add(
            repeatingTask(20) {
                if (!player.isOnline || player.isDead) {
                    return@repeatingTask
                }

                val timeSinceLastDamage = System.currentTimeMillis() - (lastDamageTime[player.uniqueId] ?: 0L)
                
                // Start decaying after delay
                if (timeSinceLastDamage >= energyDecayDelayMs) {
                    val currentEnergy = getEnergy(player, energyManager)
                    if (currentEnergy > 0) {
                        val newEnergy = (currentEnergy - energyDecayRate).coerceAtLeast(0.0)
                        energyManager?.setEnergy(newEnergy)
                    }
                }
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
        val chargeProgress = min(0.9999F, charge.toFloat() / maxCharge.toFloat())
        energyManager?.setOverlay(chargeProgress, id)
        if (energyManager == null) {
            player.exp = chargeProgress
        }
        player.playSound(player.eyeLocation, Sound.BLOCK_DISPENSER_FAIL, 0.5f, 1 + 0.1f * charge)
        
        // Show particle indicator when charging
        Particle.DUST
            .builder()
            .location(player.eyeLocation.add(player.location.direction.normalize().multiply(0.5)))
            .count(3)
            .offset(0.1, 0.1, 0.1)
            .extra(0.0)
            .data(Particle.DustOptions(org.bukkit.Color.fromRGB(139, 0, 0), 0.8f))
            .receivers(96, true)
            .spawn()
    }

    private fun finishCharging() {
        chargeRunnable?.let {
            runnables.remove(it)
            it.cancel()
        }
        chargeRunnable = null
        energyManager?.clearOverlay(id)
        if (energyManager == null) {
            player.exp = 0f
        }
        charge = 0
    }

    override fun teardown() {
        finishCharging()
        firedArrows.clear()
        clearEnergy(player)
        super.teardown()
    }
}
