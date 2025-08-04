package dev.betrix.superSmashMobsBrawl.kits.instances

import dev.betrix.superSmashMobsBrawl.disguises.CreeperDisguise
import dev.betrix.superSmashMobsBrawl.kits.definitions.KitDefinition
import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import org.bukkit.entity.Player

class CreeperKitInstance(
    definition: KitDefinition,
    player: Player,
    minigameDefinition: MinigameDefinition? = null,
) : KitInstance(definition, player, minigameDefinition) {
    override fun setup() {
        disguise = CreeperDisguise(player)
        super.setup()
    }
}
