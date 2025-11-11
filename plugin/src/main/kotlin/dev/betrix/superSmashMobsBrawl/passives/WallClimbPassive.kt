package dev.betrix.superSmashMobsBrawl.passives

import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.koin.core.component.KoinComponent

class WallClimbPassive(player: Player) : BrawlPassive("wall_climb", player), KoinComponent {

    private val power: Double
        get() = metadata.double("wallClimbPower") ?: 0.4

    private val energyPerUse: Double
        get() = metadata.double("energyPerUse") ?: 0.5

    private val cooldownMs: Long
        get() = metadata.long("cooldownMs") ?: 75L

    private val doubleJumpCooldownMs: Long
        get() = metadata.long("doubleJumpCooldownMs") ?: 150L

    private val blockCheckRadius: Int
        get() = metadata.int("blockCheckRadius") ?: 1

    var lastCooldownTime: Long = 0L
    private var lastUseTime: Long = 0L
    private var chargedDoubleJump: Boolean = false

    override fun setup() {
        super.setup()
        setupClimbTask()
    }

    private fun setupClimbTask() {
        runnables.add(
            repeatingTask(1) {
                // Reset charged double jump when on ground
                if (player.isOnGround) {
                    chargedDoubleJump = false
                }

                // Check if player is sneaking
                if (!player.isSneaking) {
                    return@repeatingTask
                }

                // Check and activate climbing
                checkAndActivate()
            }
        )
    }

    private fun checkAndActivate() {
        // Check double jump cooldown
        if (System.currentTimeMillis() - lastCooldownTime < doubleJumpCooldownMs) {
            return
        }

        // Check normal cooldown
        if (System.currentTimeMillis() - lastUseTime < cooldownMs) {
            return
        }

        // Check if player has enough energy
        if (!energyManager?.hasEnergy(energyPerUse) ?: false) {
            return
        }

        // Check for nearby solid blocks (walls)
        if (!hasNearbyWall()) {
            return
        }

        // Activate climbing
        activate()
    }

    private fun hasNearbyWall(): Boolean {
        val location = player.location
        val radius = blockCheckRadius

        for (x in -radius..radius) {
            for (y in -radius..radius) {
                for (z in -radius..radius) {
                    val block = location.clone().add(x.toDouble(), y.toDouble(), z.toDouble()).block

                    if (block.type.isSolid && !block.isLiquid) {
                        return true
                    }
                }
            }
        }

        return false
    }

    private fun activate() {
        lastUseTime = System.currentTimeMillis()

        // Apply upward velocity
        player.velocity = Vector(0.0, power, 0.0)

        // Deduct energy using energy manager
        energyManager?.consumeEnergy(energyPerUse)

        // Recharge double jump if not already charged
        if (!chargedDoubleJump) {
            rechargeDoubleJump()
            chargedDoubleJump = true
        }
    }

    private fun rechargeDoubleJump() {
        // Find the spider leap passive (or any energy double jump passive)
        val kit = kitService.getKitForPlayer(player)
        val spiderLeapPassive = kit?.getPassive("spider_leap") as? EnergyDoubleJumpPassive

        if (spiderLeapPassive != null) {
            // Recharge the double jump by allowing flight
            player.allowFlight = true
        }
    }

    fun onSpiderLeapUsed() {
        // Reset wall climb cooldown when spider leap is used
        lastCooldownTime = System.currentTimeMillis()
    }
}
