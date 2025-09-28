package dev.betrix.superSmashMobsBrawl.passives

import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import org.bukkit.entity.Player

class GigaSlimePassive(player: Player) : BrawlPassive("giga_slime", player) {
    override fun setup() {
        super.setup()
        player.sendDebugMessage("Giga Slime passive setup pending implementation")
    }

    override fun teardown() {
        player.sendDebugMessage("Giga Slime passive teardown pending implementation")
        super.teardown()
    }
}
