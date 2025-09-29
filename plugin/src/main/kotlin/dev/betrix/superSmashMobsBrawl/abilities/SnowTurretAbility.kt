package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import org.bukkit.entity.Player

class SnowTurretAbility(player: Player) : BrawlAbility("snow_turret", player) {
    override fun activate() {
        super.activate()
        player.sendDebugMessage("Snow Turret ability implementation pending")
        // TODO("")
    }
}

