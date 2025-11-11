package dev.betrix.superSmashMobsBrawl.passives

import dev.betrix.superSmashMobsBrawl.extensions.setVelocity
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

    private val energyPerFlap: Double
        get() = metadata.double("energyPerFlap") ?: 12.5

    private val minEnergyToFlap: Double
        get() = metadata.double("minEnergyToFlap") ?: 12.5

    override fun canUseJump(): Boolean {
        return energyManager?.hasEnergy(minEnergyToFlap) ?: false
    }

    override fun activate() {
        player.setVelocity(
            player.location.direction,
            power,
            true,
            power,
            verticalBoost,
            height,
            true,
        )

        // Deduct energy using energy manager
        energyManager?.consumeEnergy(energyPerFlap)
    }

    override fun playDoubleJumpSound() {
        val energyPercent = energyManager?.getEnergyPercent()?.toFloat() ?: 0f
        val volume = (0.3 + energyPercent).toFloat()
        val pitch = (Math.random() / 2 + 1).toFloat()
        player.world.playSound(player.location, doubleJumpSound, volume, pitch)
    }
}
