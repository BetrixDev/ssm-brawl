package dev.betrix.superSmashMobsBrawl.abilities.definitions

import dev.betrix.superSmashMobsBrawl.abilities.AbilityType
import dev.betrix.superSmashMobsBrawl.abilities.AbilityUsageType
import dev.betrix.superSmashMobsBrawl.abilities.ability
import dev.betrix.superSmashMobsBrawl.abilities.instances.BoneExplosionAbilityInstance
import gg.flyte.twilight.extension.name
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object BoneExplosionAbilityDefinition : AbilityDefinition() {
    override val name = "Bone Explosion"
    override val id = "bone_explosion"

    override val metadata = ability {
        description = "Releases an explosion of bones from your body, repelling all nearby enemies."
        cooldown = 10
        hotbarItemSlot = 0
        type = AbilityType.AOE
        usageType = AbilityUsageType.RIGHT_CLICK

        val hotbarItemStack = ItemStack.of(Material.IRON_AXE)
        hotbarItemStack.name(name)

        hotbarItem = hotbarItemStack
    }

    override fun createInstance(player: Player): BoneExplosionAbilityInstance {
        return BoneExplosionAbilityInstance(this, player)
    }
}
