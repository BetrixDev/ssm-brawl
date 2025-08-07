package dev.betrix.superSmashMobsBrawl.abilities

import org.bukkit.inventory.ItemStack

/** Runtime specification for an ability, built from data files. */
data class AbilitySpec(
    val id: String,
    val name: String,
    val metadata: AbilityMetadata,
)