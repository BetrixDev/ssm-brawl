package dev.betrix.superSmashMobsBrawl.passives.instances

import dev.betrix.superSmashMobsBrawl.lifecycle.Manageable
import dev.betrix.superSmashMobsBrawl.passives.definitions.PassiveDefinition
import org.bukkit.entity.Player

abstract class PassiveInstance(val definition: PassiveDefinition, val player: Player) : Manageable {
    abstract override fun setup(): Unit

    abstract override fun teardown(): Unit
}
