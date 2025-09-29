package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import org.bukkit.entity.Player

class IcePathAbility(player: Player) : BrawlAbility("ice_path", player) {
    override fun activate() {
        super.activate()
        player.sendDebugMessage("Ice Path ability implementation pending")
    }
}
