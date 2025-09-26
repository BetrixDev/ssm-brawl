package dev.betrix.superSmashMobsBrawl.models.brawlData

import com.charleskorn.kaml.YamlScalar
import kotlinx.serialization.Serializable

@Serializable data class AbilityDefFile(val abilities: List<AbilityDef>)

@Serializable
data class AbilityDef(
    val id: String,
    val cooldown: Double,
    val type: AbilityType,
    val itemSlot: Int,
    val usage: AbilityUsage,
    val hotbarItem: String,
    val displayItem: String,
    val metadata: Map<String, YamlScalar>? = null,
)

@Serializable
enum class AbilityType {
    AOE,
    PROJECTILE,
    RECOVERY,
    MELEE,
}

@Serializable
enum class AbilityUsage {
    RIGHT_CLICK,
    LEFT_CLICK,
}
