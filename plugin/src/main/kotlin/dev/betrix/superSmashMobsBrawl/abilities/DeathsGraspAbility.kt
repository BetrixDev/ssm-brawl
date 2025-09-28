package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import org.bukkit.entity.Player

class DeathsGraspAbility(player: Player) : BrawlAbility("deaths_grasp", player) {
    override fun activate() {
        super.activate()
        player.sendDebugMessage("Death's Grasp pending implementation")
    }
}

