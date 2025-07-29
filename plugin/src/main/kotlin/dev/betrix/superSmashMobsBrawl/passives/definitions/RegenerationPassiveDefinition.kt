package dev.betrix.superSmashMobsBrawl.passives.definitions

import dev.betrix.superSmashMobsBrawl.passives.instances.RegenerationPassiveInstance
import dev.betrix.superSmashMobsBrawl.passives.passive
import org.bukkit.entity.Player

object RegenerationPassiveDefinition : PassiveDefinition() {
    override val name = "Regeneration"
    override val id = "regeneration"

    override val metadata = passive {
        description = "Periodically restores health to the player"
        userFacing = false
    }

    override fun createInstance(player: Player): RegenerationPassiveInstance {
        return RegenerationPassiveInstance(this, player)
    }
}