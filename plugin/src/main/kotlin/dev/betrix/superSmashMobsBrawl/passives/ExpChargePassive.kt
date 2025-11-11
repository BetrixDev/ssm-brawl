package dev.betrix.superSmashMobsBrawl.passives

import dev.betrix.superSmashMobsBrawl.utils.isOnGround
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.entity.Player

class ExpChargePassive(player: Player) : BrawlPassive("exp_charge", player) {

    private val expAdd: Double
        get() = metadata.double("expAdd") ?: 0.01

    private val delayTicks: Long
        get() = metadata.long("delayTicks") ?: 1L

    private val chargeWhenInAir: Boolean
        get() = metadata.boolean("chargeWhenInAir") ?: false

    private val chargeWhenSneaking: Boolean
        get() = metadata.boolean("chargeWhenSneaking") ?: true

    private val startFullEnergy: Boolean
        get() = metadata.boolean("startFullEnergy") ?: false

    private val enabled: Boolean
        get() = metadata.boolean("enabled") ?: true

    override fun setup() {
        // Set full energy if configured
        if (startFullEnergy) {
            energyManager?.setEnergy(energyManager?.getMaxEnergy() ?: 100.0)
        }

        // Setup the repeating task to charge energy
        val chargeTask =
            repeatingTask(delayTicks) {
                if (!player.isOnline || player.isDead) {
                    cancel()
                    return@repeatingTask
                }

                checkAndActivate()
            }
        runnables.add(chargeTask)

        super.setup()
    }

    private fun checkAndActivate() {
        // Check if charging when not on ground is disabled
        if (!chargeWhenInAir && !isOnGround(player)) {
            return
        }

        // Check if charging when sneaking is disabled
        if (!chargeWhenSneaking && player.isSneaking) {
            return
        }

        activate()
    }

    private fun activate() {
        if (enabled && !player.isDead) {
            // Convert expAdd (0-1 range) to actual energy amount
            val maxEnergy = energyManager?.getMaxEnergy() ?: 100.0
            val energyToAdd = expAdd * maxEnergy
            energyManager?.addEnergy(energyToAdd)
        }
    }
}

