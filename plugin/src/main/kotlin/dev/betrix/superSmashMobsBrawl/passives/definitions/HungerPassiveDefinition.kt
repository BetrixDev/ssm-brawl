package dev.betrix.superSmashMobsBrawl.passives.definitions

import dev.betrix.superSmashMobsBrawl.passives.instances.HungerInstance
import dev.betrix.superSmashMobsBrawl.passives.passive
import org.bukkit.entity.Player

object HungerPassiveDefinition : PassiveDefinition() {
    override val name = "Hunger"
    override val id = "hunger"

    override val metadata = passive {
        description = "Prevents hunger loss and maintains full food level"
        userFacing = false
    }

    override fun createInstance(player: Player): HungerInstance {
        return HungerInstance(this, player)
    }
}