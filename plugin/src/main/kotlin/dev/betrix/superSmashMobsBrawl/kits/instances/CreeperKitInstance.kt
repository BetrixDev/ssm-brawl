package dev.betrix.superSmashMobsBrawl.kits.instances

import dev.betrix.superSmashMobsBrawl.kits.definitions.KitDefinition
import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import org.bukkit.entity.Player

class CreeperKitInstance(
    definition: KitDefinition, 
    player: Player,
    minigameDefinition: MinigameDefinition? = null
) : KitInstance(definition, player, minigameDefinition) {}
