package dev.betrix.superSmashMobsBrawl.abilities.definitions

import dev.betrix.superSmashMobsBrawl.abilities.AbilityType
import dev.betrix.superSmashMobsBrawl.abilities.ability
import dev.betrix.superSmashMobsBrawl.abilities.instances.StealthAbilityInstance
import gg.flyte.twilight.extension.name
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object StealthAbilityDefinition : AbilityDefinition() {
    override val name = "Stealth"
    override val id = "stealth"

    override val metadata = ability {
        description = "Become invisible and gain speed boost for a short duration"
        type = AbilityType.STEALTH
        cooldown = 12
        hotbarItemSlot = 8

        val hotbarItemStack = ItemStack.of(Material.GLASS)
        hotbarItemStack.name(name)

        hotbarItem = hotbarItemStack
    }

    override fun createInstance(player: Player): StealthAbilityInstance {
        return StealthAbilityInstance(this, player)
    }
}
