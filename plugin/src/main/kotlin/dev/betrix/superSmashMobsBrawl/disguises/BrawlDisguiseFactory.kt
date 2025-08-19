package dev.betrix.superSmashMobsBrawl.disguises

import org.bukkit.entity.Player

object BrawlDisguiseFactory {
    fun create(player: Player, id: String): BrawlDisguise? {
        val normalized = id.lowercase().replace(" ", "_").replace("-", "_")
        return when (normalized) {
            "creeper" -> CreeperDisguise(player)
            "skeleton" -> SkeletonDisguise(player)
            "cow" -> CowDisguise(player)
            "enderman" -> EndermanDisguise(player)

            "mooshroom",
            "mushroom_cow" -> MooshroomDisguise(player)
            "villager" -> VillagerDisguise(player)
            "sheep" -> SheepDisguise(player)
            "blaze" -> BlazeDisguise(player)
            "guardian" -> GuardianDisguise(player)
            "pig" -> PigDisguise(player)
            "horse" -> HorseDisguise(player)
            "zombie" -> ZombieDisguise(player)
            "wither_skeleton" -> WitherSkeletonDisguise(player)
            "witch" -> WitchDisguise(player)
            "magma_cube" -> MagmaCubeDisguise(player)
            "wolf" -> WolfDisguise(player)
            "snowman",
            "snow_golem" -> SnowmanDisguise(player)
            "squid" -> SquidDisguise(player)
            "slime" -> SlimeDisguise(player)
            "spider" -> SpiderDisguise(player)
            "iron_golem" -> IronGolemDisguise(player)
            else -> null
        }
    }
}
