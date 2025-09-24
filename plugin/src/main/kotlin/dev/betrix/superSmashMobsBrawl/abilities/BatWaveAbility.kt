package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import org.bukkit.entity.Player

class BatWaveAbility(player: Player) : BrawlAbility("bat_wave", player) {

    override fun activate() {
        super.activate()
        player.sendDebugMessage("Bat Wave pending implementation")
    }
}


