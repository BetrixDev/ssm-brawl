package dev.betrix.superSmashMobsBrawl.passives

import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import org.bukkit.entity.Player

class ArcticAuraPassive(player: Player) : BrawlPassive("arctic_aura", player) {
    override fun setup() {
        super.setup()
        player.sendDebugMessage("Arctic Aura passive setup pending implementation")
    }

    override fun teardown() {
        player.sendDebugMessage("Arctic Aura passive teardown pending implementation")
        super.teardown()
    }
}
