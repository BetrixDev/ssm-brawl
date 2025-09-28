package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import org.bukkit.entity.Player

class BileBlasterAbility(player: Player) : BrawlAbility("bile_blaster", player) {
    override fun activate() {
        super.activate()
        player.sendDebugMessage("Bile Blaster pending implementation")
    }
}
