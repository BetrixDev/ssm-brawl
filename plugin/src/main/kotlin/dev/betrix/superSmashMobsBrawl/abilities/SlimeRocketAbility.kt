package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import org.bukkit.entity.Player

class SlimeRocketAbility(player: Player) : BrawlAbility("slime_rocket", player) {
    override fun activate() {
        super.activate()
        player.sendDebugMessage("Slime Rocket ability pending implementation")
    }
}
