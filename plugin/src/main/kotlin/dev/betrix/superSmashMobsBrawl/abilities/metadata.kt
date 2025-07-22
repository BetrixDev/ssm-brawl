package dev.betrix.superSmashMobsBrawl.abilities

import org.bukkit.inventory.ItemStack

data class AbilityMetadata(
    val description: String = "",
    val type: AbilityType = AbilityType.PROJECTILE,
    val cooldown: Int = 0,
    val hotbarItem: ItemStack,
    var hotbarItemSlot: Int
)

enum class AbilityType { 
    PROJECTILE,
    TELEPORT,
    SELF_DESTRUCT,
    STEALTH
}

class AbilityBuilder {
    var description = ""
    var type = AbilityType.PROJECTILE
    var cooldown = 0
    var hotbarItemSlot: Int = 0
    lateinit var hotbarItem: ItemStack

    fun build(): AbilityMetadata {
        if (!::hotbarItem.isInitialized) {
            throw IllegalStateException("Hotbar item not set! Please set it to the variable hotbarItem in your ability metadata builder.")
        }

        return AbilityMetadata(
            description = description,
            type = type,
            cooldown = cooldown,
            hotbarItem = hotbarItem,
            hotbarItemSlot = hotbarItemSlot
        )
    }
}

fun ability(block: AbilityBuilder.() -> Unit): AbilityMetadata {
    return AbilityBuilder().apply(block).build()
}