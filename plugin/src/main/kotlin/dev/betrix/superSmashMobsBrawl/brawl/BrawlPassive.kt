package dev.betrix.superSmashMobsBrawl.brawl

import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import dev.betrix.superSmashMobsBrawl.passives.PassiveMetadata
import org.bukkit.entity.Player

abstract class BrawlPassive(
    val id: String,
    val player: Player,
    val metadata: PassiveMetadata,
    val config: Map<String, Any?> = emptyMap(),
) : Manageable() {

    override fun setup() {
        super.setup()
        if (metadata.userFacing) {
            player.sendDebugMessage("You have been given the ${id} passive")
        }
    }

    override fun teardown() {
        super.teardown()
        if (metadata.userFacing) {
            player.sendDebugMessage("The ${id} passive has been removed")
        }
    }
}
