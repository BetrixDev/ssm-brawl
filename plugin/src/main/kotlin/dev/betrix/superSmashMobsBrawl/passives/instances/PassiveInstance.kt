package dev.betrix.superSmashMobsBrawl.passives.instances

import dev.betrix.superSmashMobsBrawl.passives.definitions.PassiveDefinition
import org.bukkit.entity.Player

abstract class PassiveInstance(val definition: PassiveDefinition, val player: Player) {
    abstract fun setup(): Unit

    abstract fun teardown(): Unit
}
