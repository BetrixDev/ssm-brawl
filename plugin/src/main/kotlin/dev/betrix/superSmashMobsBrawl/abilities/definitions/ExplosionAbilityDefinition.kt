package dev.betrix.superSmashMobsBrawl.abilities.definitions

import dev.betrix.superSmashMobsBrawl.abilities.AbilityType
import dev.betrix.superSmashMobsBrawl.abilities.AbilityUsageType
import dev.betrix.superSmashMobsBrawl.abilities.ability
import dev.betrix.superSmashMobsBrawl.abilities.instances.ExplosionAbilityInstance
import gg.flyte.twilight.extension.name
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object ExplosionAbilityDefinition : AbilityDefinition() {
    override val name = "Explosion"
    override val id = "explosion"

    override val metadata = ability {
        description = "Detonate yourself dealing massive damage and knockback to nearby enemies"
        type = AbilityType.AOE
        cooldown = 8
        hotbarItemSlot = 1
        usageType = AbilityUsageType.RIGHT_CLICK

        val hotbarItemStack = ItemStack.of(Material.IRON_SHOVEL)
        hotbarItemStack.name(name)

        hotbarItem = hotbarItemStack
    }

    override fun createInstance(player: Player): ExplosionAbilityInstance {
        return ExplosionAbilityInstance(this, player)
    }
}
