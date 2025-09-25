package dev.betrix.superSmashMobsBrawl.passives

import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import org.bukkit.entity.Player

class NetherPigPassive(player: Player) : BrawlPassive("nether_pig", player) {
    private val lowHealthThreshold: Double = metadata.double("lowHealthThreshold") ?: 4.0

    override fun setup() {
        super.setup()
        player.sendDebugMessage("Nether Pig passive pending implementation. Threshold: $lowHealthThreshold hearts.")
    }

    override fun teardown() {
        player.sendDebugMessage("Nether Pig passive teardown pending implementation.")
        super.teardown()
    }
}

