package dev.betrix.superSmashMobsBrawl.passives

import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import dev.betrix.superSmashMobsBrawl.extensions.setVelocity
import dev.betrix.superSmashMobsBrawl.services.KitService
import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.repeatingTask
import kotlin.math.max
import kotlin.math.min
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerToggleFlightEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SpiderLeapPassive(player: Player) : EnergyDoubleJumpPassive("spider_leap", player), KoinComponent {

    override val power: Double
        get() = metadata.double("spiderLeapPower") ?: 1.0
    
    override val height: Double
        get() = metadata.double("spiderLeapHeight") ?: 0.9
    
    override val doubleJumpSound: Sound = Sound.ENTITY_SPIDER_AMBIENT
    
    private val energyPerJump: Float
        get() = (metadata.double("energyPerJump") ?: 16.6).toFloat()
    
    private val minEnergyToJump: Float
        get() = (metadata.double("minEnergyToJump") ?: 16.6).toFloat()
    
    private val maxEnergy: Float
        get() = (metadata.double("maxEnergy") ?: 100.0).toFloat()
    
    private val energyRegenRate: Float
        get() = (metadata.double("energyRegenRate") ?: 15.0).toFloat()

    override fun setup() {
        super.setup()
        setupEnergyRegeneration()
    }

    override fun setupRechargeTask() {
        // Override to only recharge when on ground, not in air with energy
        runnables.add(
            repeatingTask(1) {
                if (player.gameMode == GameMode.CREATIVE) {
                    return@repeatingTask
                }

                // Only recharge when on ground (not based on energy while in air)
                if (groundCheck()) {
                    canDoubleJump = true
                    // Only allow flight if we have enough energy
                    player.allowFlight = canUseJump()
                }
            }
        )
    }

    override fun setupFlightToggleListener() {
        listeners.add(
            event<PlayerToggleFlightEvent> ToggleFlightEvent@{
                if (
                    player != this@SpiderLeapPassive.player ||
                        player.gameMode == GameMode.CREATIVE ||
                        player.gameMode == GameMode.SPECTATOR
                ) {
                    return@ToggleFlightEvent
                }

                isCancelled = true

                // Check if double jump is available
                if (!canDoubleJump) {
                    player.sendDebugMessage("[Spider Leap] Double jump not available")
                    player.allowFlight = false
                    return@ToggleFlightEvent
                }

                // For spider leap, MUST have enough energy to jump
                if (!canUseJump()) {
                    player.sendDebugMessage("[Spider Leap] Not enough energy to jump")
                    player.allowFlight = false
                    return@ToggleFlightEvent
                }

                player.sendDebugMessage("[Spider Leap] Activated")

                player.isFlying = false
                player.allowFlight = false
                player.fallDistance = 0f

                playDoubleJumpSound()
                activate()

                canDoubleJump = false
                lastJumpTimeMs = System.currentTimeMillis()
            }
        )
    }

    private fun setupEnergyRegeneration() {
        runnables.add(
            repeatingTask(1) {
                val currentEnergy = player.exp * maxEnergy
                val newEnergy = min(maxEnergy, currentEnergy + (energyRegenRate / 20f))
                player.exp = min(0.9999f, newEnergy / maxEnergy)
            }
        )
    }
    
    override fun canUseJump(): Boolean {
        val currentEnergy = player.exp * maxEnergy
        return currentEnergy >= minEnergyToJump
    }

    override fun activate() {
        // Spider leap goes exactly in the direction the player is looking
        player.setVelocity(player.location.direction, power, false, 0.0, 0.2, height, true)
        
        // Deduct energy (convert from percentage to actual value)
        val currentEnergyPercent = player.exp
        val energyPerJumpPercent = energyPerJump / maxEnergy
        val newEnergyPercent = max(0f, currentEnergyPercent - energyPerJumpPercent)
        player.exp = newEnergyPercent
        
        // Interact with wall climb passive if present
        val kit = kitService.getKitForPlayer(player)
        val wallClimbPassive = kit?.getPassive("wall_climb") as? WallClimbPassive
        wallClimbPassive?.onSpiderLeapUsed()
    }

    override fun playDoubleJumpSound() {
        player.world.playSound(player.location, doubleJumpSound, 1f, 1.5f)
    }
}
