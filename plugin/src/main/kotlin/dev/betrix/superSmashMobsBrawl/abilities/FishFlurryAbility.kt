package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import org.bukkit.entity.Player

class FishFlurryAbility(player: Player) : BrawlAbility("fish_flurry", player) {
    override fun activate() {
        super.activate()
        player.sendDebugMessage("Fish Flurry ability is pending implementation")
    }
}
