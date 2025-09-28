package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import org.bukkit.entity.Player

class TargetLaserAbility(player: Player) : BrawlAbility("target_laser", player) {
    override fun activate() {
        super.activate()
        player.sendDebugMessage("Target Laser ability pending implementation")
    }
}

