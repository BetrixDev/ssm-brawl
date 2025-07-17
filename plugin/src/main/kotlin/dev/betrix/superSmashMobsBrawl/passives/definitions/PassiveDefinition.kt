package dev.betrix.superSmashMobsBrawl.passives.definitions

import dev.betrix.superSmashMobsBrawl.passives.PassiveMetadata
import dev.betrix.superSmashMobsBrawl.passives.instances.PassiveInstance
import org.bukkit.entity.Player

abstract class PassiveDefinition {
    abstract val name: String
    abstract val id: String
    abstract val metadata: PassiveMetadata

    abstract fun createInstance(player: Player): PassiveInstance
}