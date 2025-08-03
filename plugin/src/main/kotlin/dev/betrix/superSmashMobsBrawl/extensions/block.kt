package dev.betrix.superSmashMobsBrawl.extensions

import org.bukkit.Material
import org.bukkit.block.Block

fun Block.isAirOrFoliage(): Boolean {
    return when (type) {
        // Air blocks
        Material.AIR,
        Material.VOID_AIR,
        Material.CAVE_AIR,

        // Leaves
        Material.OAK_LEAVES,
        Material.SPRUCE_LEAVES,
        Material.BIRCH_LEAVES,
        Material.JUNGLE_LEAVES,
        Material.ACACIA_LEAVES,
        Material.DARK_OAK_LEAVES,
        Material.MANGROVE_LEAVES,
        Material.CHERRY_LEAVES,
        Material.AZALEA_LEAVES,
        Material.FLOWERING_AZALEA_LEAVES,

        // Grass and ferns
        Material.FERN,
        Material.DEAD_BUSH,
        Material.SEAGRASS,
        Material.TALL_SEAGRASS,
        Material.TALL_GRASS,
        Material.LARGE_FERN,

        // Flowers
        Material.DANDELION,
        Material.POPPY,
        Material.BLUE_ORCHID,
        Material.ALLIUM,
        Material.AZURE_BLUET,
        Material.RED_TULIP,
        Material.ORANGE_TULIP,
        Material.WHITE_TULIP,
        Material.PINK_TULIP,
        Material.OXEYE_DAISY,
        Material.CORNFLOWER,
        Material.LILY_OF_THE_VALLEY,
        Material.WITHER_ROSE,
        Material.SUNFLOWER,
        Material.LILAC,
        Material.ROSE_BUSH,
        Material.PEONY,
        Material.PITCHER_PLANT,
        Material.TORCHFLOWER,

        // Crops and plants
        Material.WHEAT,
        Material.CARROTS,
        Material.POTATOES,
        Material.BEETROOTS,
        Material.MELON_STEM,
        Material.PUMPKIN_STEM,
        Material.ATTACHED_MELON_STEM,
        Material.ATTACHED_PUMPKIN_STEM,
        Material.SWEET_BERRY_BUSH,
        Material.COCOA,
        Material.NETHER_WART,
        Material.TORCHFLOWER_CROP,
        Material.PITCHER_CROP,

        // Vines and climbing plants
        Material.VINE,
        Material.WEEPING_VINES,
        Material.WEEPING_VINES_PLANT,
        Material.TWISTING_VINES,
        Material.TWISTING_VINES_PLANT,
        Material.CAVE_VINES,
        Material.CAVE_VINES_PLANT,

        // Kelp and sea plants
        Material.KELP,
        Material.KELP_PLANT,
        Material.SEA_PICKLE,

        // Saplings
        Material.OAK_SAPLING,
        Material.SPRUCE_SAPLING,
        Material.BIRCH_SAPLING,
        Material.JUNGLE_SAPLING,
        Material.ACACIA_SAPLING,
        Material.DARK_OAK_SAPLING,
        Material.MANGROVE_PROPAGULE,
        Material.CHERRY_SAPLING,
        Material.AZALEA,
        Material.FLOWERING_AZALEA,

        // Mushrooms
        Material.BROWN_MUSHROOM,
        Material.RED_MUSHROOM,
        Material.CRIMSON_FUNGUS,
        Material.WARPED_FUNGUS,
        Material.CRIMSON_ROOTS,
        Material.WARPED_ROOTS,
        Material.NETHER_SPROUTS,

        // Moss and other organic blocks
        Material.MOSS_CARPET,
        Material.MOSS_BLOCK,
        Material.HANGING_ROOTS,
        Material.SPORE_BLOSSOM,
        Material.GLOW_LICHEN,
        Material.SCULK_VEIN,

        // Water plants
        Material.LILY_PAD,
        Material.BIG_DRIPLEAF,
        Material.BIG_DRIPLEAF_STEM,
        Material.SMALL_DRIPLEAF,

        // Bamboo
        Material.BAMBOO,
        Material.BAMBOO_SAPLING,

        // Chorus plants
        Material.CHORUS_PLANT,
        Material.CHORUS_FLOWER,

        // Sugar cane and cactus (often considered foliage)
        Material.SUGAR_CANE,
        Material.CACTUS,

        // Pink petals
        Material.PINK_PETALS -> true

        else -> false
    }
}
