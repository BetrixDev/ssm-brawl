package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import org.bukkit.entity.Player

class WhirlpoolAxeAbility(player: Player) : BrawlAbility("whirlpool_axe", player) {
    override fun activate() {
        super.activate()
        player.sendDebugMessage("Whirlpool Axe ability pending implementation")
    }
}

