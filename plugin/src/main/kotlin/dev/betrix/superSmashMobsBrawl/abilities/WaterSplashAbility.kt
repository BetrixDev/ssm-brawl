package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import org.bukkit.entity.Player

class WaterSplashAbility(player: Player) : BrawlAbility("water_splash", player) {
    override fun activate() {
        super.activate()
        player.sendDebugMessage("Water Splash ability pending implementation")
    }
}

