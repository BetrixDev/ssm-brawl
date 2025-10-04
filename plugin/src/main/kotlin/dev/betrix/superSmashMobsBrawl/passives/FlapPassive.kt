package dev.betrix.superSmashMobsBrawl.passives

import dev.betrix.superSmashMobsBrawl.extensions.setVelocity
import gg.flyte.twilight.scheduler.repeatingTask
import kotlin.math.max
import kotlin.math.min
import org.bukkit.Sound
import org.bukkit.entity.Player

class FlapPassive(player: Player) : EnergyDoubleJumpPassive("flap", player) {

    override val power: Double
        get() = metadata.double("flapStrengthMultiplier") ?: 0.95
    
    override val height: Double
        get() = metadata.double("flapHeightMax") ?: 0.9
    
    private val verticalBoost: Double
        get() = metadata.double("flapVerticalBoost") ?: 0.15
    
    override val doubleJumpSound: Sound = Sound.ENTITY_CHICKEN_AMBIENT
    
    private val energyPerFlap: Float
        get() = (metadata.double("energyPerFlap") ?: 12.5).toFloat()
    
    private val minEnergyToFlap: Float
        get() = (metadata.double("minEnergyToFlap") ?: 12.5).toFloat()
    
    private val maxEnergy: Float
        get() = (metadata.double("maxEnergy") ?: 100.0).toFloat()
    
    private val energyRegenRate: Float
        get() = (metadata.double("energyRegenRate") ?: 4.0).toFloat()

    override fun setup() {
        super.setup()
        setupEnergyRegeneration()
    }

    private fun setupEnergyRegeneration() {
        runnables.add(
            repeatingTask(1) {
                // Only regenerate energy when on the ground
                if (!groundCheck()) {
                    return@repeatingTask
                }
                
                val currentEnergy = player.exp * maxEnergy
                val newEnergy = min(maxEnergy, currentEnergy + (energyRegenRate / 20f))
                player.exp = min(0.9999f, newEnergy / maxEnergy)
            }
        )
    }
    
    override fun canUseJump(): Boolean {
        val currentEnergy = player.exp * maxEnergy
        return currentEnergy >= minEnergyToFlap
    }

    override fun activate() {
        player.setVelocity(player.location.direction, power, true, power, verticalBoost, height, true)
        
        // Deduct energy (convert from percentage to actual value)
        val currentEnergyPercent = player.exp
        val energyPerFlapPercent = energyPerFlap / maxEnergy
        val newEnergyPercent = max(0f, currentEnergyPercent - energyPerFlapPercent)
        player.exp = newEnergyPercent
    }

    override fun playDoubleJumpSound() {
        val volume = (0.3 + player.exp).toFloat()
        val pitch = (Math.random() / 2 + 1).toFloat()
        player.world.playSound(player.location, doubleJumpSound, volume, pitch)
    }
}
