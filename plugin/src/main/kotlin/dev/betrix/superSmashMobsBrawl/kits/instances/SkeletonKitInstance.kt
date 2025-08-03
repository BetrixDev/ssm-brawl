package dev.betrix.superSmashMobsBrawl.kits.instances

import dev.betrix.superSmashMobsBrawl.disguises.SkeletonDisguise
import dev.betrix.superSmashMobsBrawl.kits.definitions.KitDefinition
import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import org.bukkit.entity.Player

class SkeletonKitInstance(
    definition: KitDefinition,
    player: Player,
    minigameDefinition: MinigameDefinition?,
) : KitInstance(definition, player, minigameDefinition) {
    override fun setup() {
        disguise = SkeletonDisguise(player)
        super.setup()
    }
}
