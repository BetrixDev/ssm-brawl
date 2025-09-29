package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import org.bukkit.entity.Player

class BlizzardAbility(player: Player) : BrawlAbility("blizzard", player) {
    override fun activate() {
        super.activate()
        player.sendDebugMessage("Blizzard ability implementation pending")
        // TODO("")
    }
}

