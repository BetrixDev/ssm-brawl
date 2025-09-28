package dev.betrix.superSmashMobsBrawl.passives

import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import org.bukkit.entity.Player

class ThornsPassive(player: Player) : BrawlPassive("thorns", player) {
    override fun setup() {
        super.setup()
        player.sendDebugMessage("Thorns passive setup pending implementation")
    }

    override fun teardown() {
        player.sendDebugMessage("Thorns passive teardown pending implementation")
        super.teardown()
    }
}
