package dev.betrix.superSmashMobsBrawl.passives

import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import org.bukkit.entity.Player

class CorruptedArrowPassive(player: Player) : BrawlPassive("corrupted_arrow", player) {
    override fun setup() {
        super.setup()
        player.sendDebugMessage("Corrupted Arrow passive pending implementation")
    }

    override fun teardown() {
        player.sendDebugMessage("Corrupted Arrow passive teardown pending implementation")
        super.teardown()
    }
}

