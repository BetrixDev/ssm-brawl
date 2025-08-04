package dev.betrix.superSmashMobsBrawl.passives.definitions

import dev.betrix.superSmashMobsBrawl.passives.instances.BarragePassiveInstance
import dev.betrix.superSmashMobsBrawl.passives.passive
import org.bukkit.entity.Player

object BarragePassiveDefinition : PassiveDefinition() {
    override val name = "Barrage"
    override val id = "barrage"

    override val metadata = passive {
        description = "The more you charge your bow, the more arrows you shoot!"
    }

    override fun createInstance(player: Player): BarragePassiveInstance {
        return BarragePassiveInstance(this, player)
    }
}
