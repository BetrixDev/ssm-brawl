package dev.betrix.superSmashMobsBrawl.extensions

import org.bukkit.Material

private val doorMaterials = listOf(
    Material.ACACIA_DOOR,
    Material.BAMBOO_DOOR,
    Material.BIRCH_DOOR,
    Material.CHERRY_DOOR,
    Material.CRIMSON_DOOR,
    Material.DARK_OAK_DOOR,
    Material.IRON_DOOR,
    Material.JUNGLE_DOOR,
    Material.MANGROVE_DOOR,
    Material.OAK_DOOR,
    Material.SPRUCE_DOOR,
    Material.WARPED_DOOR
)

fun Material.isDoor(): Boolean {
    return doorMaterials.contains(this)
}