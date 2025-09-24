package dev.betrix.superSmashMobsBrawl.passives

import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import org.bukkit.entity.Player

class WallClimbPassive(player: Player) : BrawlPassive("wall_climb", player) {

    override fun setup() {
        super.setup()
        player.sendDebugMessage("Wall Climb passive not implemented yet")
    }

    override fun teardown() {
        player.sendDebugMessage("Wall Climb passive teardown placeholder")
        super.teardown()
    }
}
