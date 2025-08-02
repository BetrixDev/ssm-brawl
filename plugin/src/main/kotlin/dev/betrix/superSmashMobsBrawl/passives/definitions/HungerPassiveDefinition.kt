package dev.betrix.superSmashMobsBrawl.passives.definitions

import dev.betrix.superSmashMobsBrawl.passives.instances.HungerPassiveInstance
import dev.betrix.superSmashMobsBrawl.passives.passive
import org.bukkit.entity.Player

object HungerPassiveDefinition : PassiveDefinition() {
    override val name = "Hunger"
    override val id = "hunger"

    override val metadata = passive {
        description = "Damages the player if they haven't dealt damage in a while"
        userFacing = false
    }

    override fun createInstance(player: Player): HungerPassiveInstance {
        return HungerPassiveInstance(this, player)
    }
}
