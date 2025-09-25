package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import org.bukkit.entity.Player

class InkShotgunAbility(player: Player) : BrawlAbility("ink_shotgun", player) {
    override fun activate() {
        super.activate()
        player.sendDebugMessage("Ink Shotgun ability is pending implementation")
    }
}
