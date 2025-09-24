package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import org.bukkit.entity.Player

class DazePotionAbility(player: Player) : BrawlAbility("daze_potion", player) {

    override fun activate() {
        super.activate()
        player.sendDebugMessage("Daze Potion pending implementation")
    }
}
