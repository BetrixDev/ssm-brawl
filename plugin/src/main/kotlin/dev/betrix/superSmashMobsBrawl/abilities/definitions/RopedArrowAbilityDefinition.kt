package dev.betrix.superSmashMobsBrawl.abilities.definitions

import dev.betrix.superSmashMobsBrawl.abilities.AbilityUsageType
import dev.betrix.superSmashMobsBrawl.abilities.ability
import dev.betrix.superSmashMobsBrawl.abilities.instances.RopedArrowAbilityInstance
import gg.flyte.twilight.extension.name
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object RopedArrowAbilityDefinition : AbilityDefinition() {
    override val name = "Roped Arrow"
    override val id = "roped_arrow"

    override val metadata = ability {
        description =
            "Instantly fires an arrow. When it collides with something, you are pulled towards it."
        cooldown = 5
        usageType = AbilityUsageType.LEFT_CLICK
        hotbarItemSlot = 1

        val hotbarItemStack = ItemStack.of(Material.BOW)
        hotbarItemStack.name(name)

        hotbarItem = hotbarItemStack
    }

    override fun createInstance(player: Player): RopedArrowAbilityInstance {
        return RopedArrowAbilityInstance(this, player)
    }
}
