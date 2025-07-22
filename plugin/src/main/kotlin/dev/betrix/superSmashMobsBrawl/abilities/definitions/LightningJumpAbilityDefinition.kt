package dev.betrix.superSmashMobsBrawl.abilities.definitions

import dev.betrix.superSmashMobsBrawl.abilities.AbilityType
import dev.betrix.superSmashMobsBrawl.abilities.ability
import dev.betrix.superSmashMobsBrawl.abilities.instances.AbilityInstance
import dev.betrix.superSmashMobsBrawl.abilities.instances.LightningJumpAbilityInstance
import gg.flyte.twilight.extension.name
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object LightningJumpAbilityDefinition : AbilityDefinition() {
    override val name = "Lightning Jump"
    override val id = "lightning_jump"

    override val metadata = ability {
        description = "Teleport to target location with lightning strike and damage nearby enemies"
        type = AbilityType.TELEPORT
        cooldown = 8
        hotbarItemSlot = 0

        val hotbarItemStack = ItemStack.of(Material.IRON_SWORD)
        hotbarItemStack.name(name)

        hotbarItem = hotbarItemStack
    }

    override fun createInstance(player: Player): LightningJumpAbilityInstance {
        return LightningJumpAbilityInstance(this, player)
    }
}