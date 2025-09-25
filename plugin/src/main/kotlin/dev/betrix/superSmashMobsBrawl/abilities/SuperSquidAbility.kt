package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import org.bukkit.entity.Player

class SuperSquidAbility(player: Player) : BrawlAbility("super_squid", player) {
    override fun activate() {
        super.activate()
        player.sendDebugMessage("Super Squid ability is pending implementation")
    }
}
