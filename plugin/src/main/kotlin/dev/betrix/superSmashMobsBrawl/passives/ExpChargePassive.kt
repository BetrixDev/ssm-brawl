package dev.betrix.superSmashMobsBrawl.passives

import dev.betrix.superSmashMobsBrawl.utils.isOnGround
import gg.flyte.twilight.scheduler.repeatingTask
import kotlin.math.min
import org.bukkit.entity.Player

class ExpChargePassive(player: Player) : BrawlPassive("exp_charge", player) {

    private val expAdd: Float
        get() = (metadata.double("expAdd") ?: 0.01).toFloat()

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
            player.exp = 1.0f
        }

        // Setup the repeating task to charge XP
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
            val xp = player.exp
            player.exp = min(xp + expAdd, 1.0f)
        }
    }
}

