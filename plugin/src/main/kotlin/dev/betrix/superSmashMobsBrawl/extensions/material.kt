package dev.betrix.superSmashMobsBrawl.extensions

import org.bukkit.Material

private val doorMaterials =
    listOf(
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
        Material.WARPED_DOOR,
    )

private val buttonMaterials =
    listOf(
        Material.STONE_BUTTON,
        Material.OAK_BUTTON,
        Material.SPRUCE_BUTTON,
        Material.BIRCH_BUTTON,
        Material.JUNGLE_BUTTON,
        Material.ACACIA_BUTTON,
        Material.DARK_OAK_BUTTON,
        Material.MANGROVE_BUTTON,
        Material.CHERRY_BUTTON,
        Material.BAMBOO_BUTTON,
        Material.CRIMSON_BUTTON,
        Material.WARPED_BUTTON,
        Material.POLISHED_BLACKSTONE_BUTTON,
    )

private val pressurePlateMaterials =
    listOf(
        Material.STONE_PRESSURE_PLATE,
        Material.OAK_PRESSURE_PLATE,
        Material.SPRUCE_PRESSURE_PLATE,
        Material.BIRCH_PRESSURE_PLATE,
        Material.JUNGLE_PRESSURE_PLATE,
        Material.ACACIA_PRESSURE_PLATE,
        Material.DARK_OAK_PRESSURE_PLATE,
        Material.MANGROVE_PRESSURE_PLATE,
        Material.CHERRY_PRESSURE_PLATE,
        Material.BAMBOO_PRESSURE_PLATE,
        Material.CRIMSON_PRESSURE_PLATE,
        Material.WARPED_PRESSURE_PLATE,
        Material.LIGHT_WEIGHTED_PRESSURE_PLATE,
        Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
        Material.POLISHED_BLACKSTONE_PRESSURE_PLATE,
    )

// Blocks that should be allowed for interaction in hub worlds
private val hubInteractableMaterials =
    doorMaterials +
        buttonMaterials +
        pressurePlateMaterials +
        listOf(
            Material.LEVER,
            Material.TRIPWIRE_HOOK,
            // Add any other blocks that should be interactable in hubs
        )

private val swordMaterials =
    listOf(
        Material.WOODEN_SWORD,
        Material.STONE_SWORD,
        Material.IRON_SWORD,
        Material.GOLDEN_SWORD,
        Material.DIAMOND_SWORD,
        Material.NETHERITE_SWORD,
    )

fun Material.isDoor(): Boolean {
    return doorMaterials.contains(this)
}

fun Material.isButton(): Boolean {
    return buttonMaterials.contains(this)
}

fun Material.isPressurePlate(): Boolean {
    return pressurePlateMaterials.contains(this)
}

/** Returns true if this material should be allowed for player interaction in hub worlds */
fun Material.isHubInteractable(): Boolean {
    return hubInteractableMaterials.contains(this)
}

val Material.isSword: Boolean
    get() = swordMaterials.contains(this)
