package dev.betrix.superSmashMobsBrawl.passives.definitions

import dev.betrix.superSmashMobsBrawl.passives.instances.RegenerationInstance
import dev.betrix.superSmashMobsBrawl.passives.passive
import org.bukkit.entity.Player

class RegenerationPassiveDefinition(
    private val regenerationRate: Double = 0.5,
    private val maxHealth: Double = 20.0,
    private val delayTicks: Int = 100 // 5 seconds default
) : PassiveDefinition() {
    override val name = "Regeneration"
    override val id = "regeneration"

    override val metadata = passive {
        description = "Regenerates health over time (Rate: $regenerationRate HP/5s, Max: $maxHealth HP)"
        userFacing = false
    }

    override fun createInstance(player: Player): RegenerationInstance {
        return RegenerationInstance(this, player, regenerationRate, maxHealth, delayTicks)
    }
}