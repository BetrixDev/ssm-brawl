package dev.betrix.superSmashMobsBrawl.kits

import dev.betrix.superSmashMobsBrawl.models.brawlData.KitEnergyDef
import dev.betrix.superSmashMobsBrawl.utils.isOnGround
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import kotlin.math.max
import kotlin.math.min
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class KitEnergyManager(
    private val player: Player,
    private val energyDef: KitEnergyDef,
) {
    private var currentEnergy: Double = if (energyDef.startFull) energyDef.maxEnergy else 0.0
    private val regenModifiers = ConcurrentHashMap<UUID, RegenModifier>()
    private var overlayValue: Float? = null
    private var overlayOwner: String? = null
    private var regenTask: TwilightRunnable? = null

    data class RegenModifier(
        val id: UUID,
        val multiplier: Double,
        val flatBonus: Double,
        val expiresAt: Long,
    )

    fun getCurrentEnergy(): Double = currentEnergy

    fun getMaxEnergy(): Double = energyDef.maxEnergy

    fun getEnergyPercent(): Double = currentEnergy / energyDef.maxEnergy

    fun hasEnergy(amount: Double): Boolean = currentEnergy >= amount

    fun consumeEnergy(amount: Double): Boolean {
        if (currentEnergy < amount) {
            return false
        }
        currentEnergy = max(0.0, currentEnergy - amount)
        updateXpBar()
        return true
    }

    fun addEnergy(amount: Double) {
        currentEnergy = min(energyDef.maxEnergy, currentEnergy + amount)
        updateXpBar()
    }

    fun setEnergy(amount: Double) {
        currentEnergy = amount.coerceIn(0.0, energyDef.maxEnergy)
        updateXpBar()
    }

    fun addRegenModifier(
        id: UUID,
        multiplier: Double = 1.0,
        flatBonus: Double = 0.0,
        durationMs: Long,
    ) {
        val expiresAt = System.currentTimeMillis() + durationMs
        regenModifiers[id] = RegenModifier(id, multiplier, flatBonus, expiresAt)
    }

    fun removeRegenModifier(id: UUID) {
        regenModifiers.remove(id)
    }

    fun setOverlay(value: Float?, owner: String?) {
        overlayValue = value
        overlayOwner = owner
        updateXpBar()
    }

    fun clearOverlay(owner: String) {
        if (overlayOwner == owner) {
            overlayValue = null
            overlayOwner = null
            updateXpBar()
        }
    }

    fun setup() {
        if (energyDef.startFull) {
            currentEnergy = energyDef.maxEnergy
        }
        updateXpBar()

        if (energyDef.regenRate > 0.0) {
            regenTask = repeatingTask(1) {
                if (!player.isOnline || player.isDead) {
                    cancel()
                    return@repeatingTask
                }

                // Check if regeneration should occur
                if (!energyDef.regenWhenInAir && !isOnGround(player)) {
                    return@repeatingTask
                }

                if (!energyDef.regenWhenSneaking && player.isSneaking) {
                    return@repeatingTask
                }

                // Calculate effective regen rate with modifiers
                var effectiveRegenRate = energyDef.regenRate

                // Clean up expired modifiers and apply active ones
                val now = System.currentTimeMillis()
                val expiredIds = mutableListOf<UUID>()
                regenModifiers.values.forEach { modifier ->
                    if (now >= modifier.expiresAt) {
                        expiredIds.add(modifier.id)
                    } else {
                        effectiveRegenRate = (effectiveRegenRate * modifier.multiplier) + modifier.flatBonus
                    }
                }
                expiredIds.forEach { regenModifiers.remove(it) }

                // Apply regeneration
                if (effectiveRegenRate > 0.0) {
                    val regenPerTick = effectiveRegenRate / 20.0
                    currentEnergy = min(energyDef.maxEnergy, currentEnergy + regenPerTick)
                    updateXpBar()
                }
            }
        }
    }

    fun teardown() {
        regenTask?.cancel()
        regenTask = null
        regenModifiers.clear()
        overlayValue = null
        overlayOwner = null
        player.exp = 0f
    }

    private fun updateXpBar() {
        // If there's an overlay, use that instead of energy
        val displayValue = overlayValue ?: getEnergyPercent().toFloat()
        player.exp = min(0.9999f, displayValue.coerceIn(0f, 1f))
    }
}

