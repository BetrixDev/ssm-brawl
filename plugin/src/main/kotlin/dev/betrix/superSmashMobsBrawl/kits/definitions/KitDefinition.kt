package dev.betrix.superSmashMobsBrawl.kits.definitions

import dev.betrix.superSmashMobsBrawl.kits.KitMetadata
import dev.betrix.superSmashMobsBrawl.kits.instances.KitInstance
import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import org.bukkit.entity.Player

abstract class KitDefinition {
    abstract val name: String
    abstract val id: String
    abstract val metadata: KitMetadata

    abstract fun createInstance(player: Player, minigameDefinition: MinigameDefinition? = null): KitInstance
}
