package dev.betrix.superSmashMobsBrawl.abilities.definitions

import dev.betrix.superSmashMobsBrawl.abilities.AbilityMetadata
import dev.betrix.superSmashMobsBrawl.abilities.instances.AbilityInstance
import org.bukkit.entity.Player

abstract class AbilityDefinition {
    abstract val name: String
    abstract val id: String
    abstract val metadata: AbilityMetadata

    abstract fun createInstance(player: Player): AbilityInstance
}
