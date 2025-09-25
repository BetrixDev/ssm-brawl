package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import org.bukkit.entity.Player

class BouncyBaconAbility(player: Player) : BrawlAbility("bouncy_bacon", player) {
    override fun activate() {
        super.activate()
        player.sendDebugMessage("Bouncy Bacon ability pending implementation.")
    }
}
