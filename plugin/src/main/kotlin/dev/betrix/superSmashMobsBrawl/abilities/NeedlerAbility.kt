package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import org.bukkit.entity.Player

class NeedlerAbility(player: Player) : BrawlAbility("needler", player) {

    override fun activate() {
        super.activate()
        player.sendDebugMessage("Needler ability not implemented yet")
    }
}

