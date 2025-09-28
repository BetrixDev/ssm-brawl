package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import org.bukkit.entity.Player

class SlimeSlamAbility(player: Player) : BrawlAbility("slime_slam", player) {
    override fun activate() {
        super.activate()
        player.sendDebugMessage("Slime Slam ability pending implementation")
    }
}
