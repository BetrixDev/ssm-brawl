package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import org.bukkit.entity.Player

class SpinWebAbility(player: Player) : BrawlAbility("spin_web", player) {

    override fun activate() {
        super.activate()
        player.sendDebugMessage("Spin Web ability not implemented yet")
    }
}

