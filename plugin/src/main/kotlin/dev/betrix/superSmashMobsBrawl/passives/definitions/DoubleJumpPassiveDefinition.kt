package dev.betrix.superSmashMobsBrawl.passives.definitions

import dev.betrix.superSmashMobsBrawl.passives.instances.DoubleJumpInstance
import dev.betrix.superSmashMobsBrawl.passives.passive
import org.bukkit.entity.Player

object DoubleJumpPassiveDefinition : PassiveDefinition() {
    override val name = "Double Jump"
    override val id = "double_jump"

    override val metadata = passive {
        description = "Generic Double Jump"
        userFacing = false
    }

    override fun createInstance(player: Player): DoubleJumpInstance {
        return DoubleJumpInstance(this, player)
    }
}
