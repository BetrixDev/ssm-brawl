package dev.betrix.superSmashMobsBrawl.passives

import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import org.bukkit.entity.Player

class SpiderLeapPassive(player: Player) : BrawlPassive("spider_leap", player) {

    override fun setup() {
        super.setup()
        player.sendDebugMessage("Spider Leap passive not implemented yet")
    }

    override fun teardown() {
        player.sendDebugMessage("Spider Leap passive teardown placeholder")
        super.teardown()
    }
}
