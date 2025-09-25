package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import org.bukkit.entity.Player

class BabyBaconBombAbility(player: Player) : BrawlAbility("baby_bacon_bomb", player) {
    override fun activate() {
        super.activate()
        player.sendDebugMessage("Baby Bacon Bomb ability pending implementation.")
    }
}
