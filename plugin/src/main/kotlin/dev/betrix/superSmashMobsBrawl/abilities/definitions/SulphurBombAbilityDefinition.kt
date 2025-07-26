package dev.betrix.superSmashMobsBrawl.abilities.definitions

import dev.betrix.superSmashMobsBrawl.abilities.AbilityType
import dev.betrix.superSmashMobsBrawl.abilities.ability
import dev.betrix.superSmashMobsBrawl.abilities.instances.SulphurBombAbilityInstance
import gg.flyte.twilight.extension.name
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object SulphurBombAbilityDefinition : AbilityDefinition() {
    override val name = "Sulphur Bomb"
    override val id = "sulphur_bomb"

    override val metadata = ability {
        description =
            "Throws a coal projectile that explodes on impact, dealing damage and knockback"
        type = AbilityType.PROJECTILE
        cooldown = 3
        hotbarItemSlot = 1

        val hotbarItemStack = ItemStack.of(Material.IRON_AXE)
        hotbarItemStack.name(name)

        hotbarItem = hotbarItemStack
    }

    override fun createInstance(player: Player): SulphurBombAbilityInstance {
        return SulphurBombAbilityInstance(this, player)
    }
}
