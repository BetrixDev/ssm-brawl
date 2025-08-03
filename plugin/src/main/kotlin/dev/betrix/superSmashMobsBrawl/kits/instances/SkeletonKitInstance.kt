package dev.betrix.superSmashMobsBrawl.kits.instances

import dev.betrix.superSmashMobsBrawl.disguises.SkeletonDisguise
import dev.betrix.superSmashMobsBrawl.kits.definitions.KitDefinition
import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class SkeletonKitInstance(
    definition: KitDefinition,
    player: Player,
    minigameDefinition: MinigameDefinition?,
) : KitInstance(definition, player, minigameDefinition) {
    private val arrowHotbarSlot = 2
    private val maximumArrowCount = 3

    override fun setup() {
        disguise = SkeletonDisguise(player)

        super.setup()

        val arrows = ItemStack.of(Material.ARROW).apply {
            amount = maximumArrowCount
        }

        player.inventory.setItem(arrowHotbarSlot, arrows)
    }
}
