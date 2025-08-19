package dev.betrix.superSmashMobsBrawl.disguises

import org.bukkit.entity.Player

private val disguiseConstructors: Map<String, (Player) -> BrawlDisguise> = mapOf(
    "creeper" to ::CreeperDisguise,
    "skeleton" to ::SkeletonDisguise,
    "cow" to ::CowDisguise,
    "enderman" to ::EndermanDisguise,
    "mooshroom" to ::MooshroomDisguise,
    "mushroom_cow" to ::MooshroomDisguise,
    "villager" to ::VillagerDisguise,
    "sheep" to ::SheepDisguise,
    "blaze" to ::BlazeDisguise,
    "guardian" to ::GuardianDisguise,
    "pig" to ::PigDisguise,
    "horse" to ::HorseDisguise,
    "zombie" to ::ZombieDisguise,
    "wither_skeleton" to ::WitherSkeletonDisguise,
    "witch" to ::WitchDisguise,
    "magma_cube" to ::MagmaCubeDisguise,
    "wolf" to ::WolfDisguise,
    "snowman" to ::SnowmanDisguise,
    "snow_golem" to ::SnowmanDisguise,
    "squid" to ::SquidDisguise,
    "slime" to ::SlimeDisguise,
    "spider" to ::SpiderDisguise,
    "iron_golem" to ::IronGolemDisguise,
)

object BrawlDisguiseFactory {
    fun create(player: Player, id: String): BrawlDisguise? {
        val normalized = id.lowercase().replace(" ", "_").replace("-", "_")
        return disguiseConstructors[normalized]?.invoke(player)
    }
}
