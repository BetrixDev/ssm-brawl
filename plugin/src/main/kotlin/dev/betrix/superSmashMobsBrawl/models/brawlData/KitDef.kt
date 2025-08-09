package dev.betrix.superSmashMobsBrawl.models.brawlData

import com.charleskorn.kaml.YamlNode
import kotlinx.serialization.Serializable

@Serializable data class KitDefFile(val kits: List<KitDef>)

@Serializable
data class KitDef(
    val id: String,
    val meleeDamage: Double,
    val armor: Double,
    val knockbackMultiplier: Double,
    val disguiseId: String? = null,
    val passives: List<KitPassiveDef> = listOf(),
    val abilities: List<KitAbilityDef> = listOf(),
    val userFacing: Boolean = true,
)

@Serializable
data class KitPassiveDef(val id: String, val overrides: KitPassiveDefOverride? = null)

@Serializable data class KitPassiveDefOverride(val metadata: Map<String, YamlNode>? = null)

@Serializable
data class KitAbilityDef(val id: String, val overrides: KitAbilityDefOverride? = null)

@Serializable data class KitAbilityDefOverride(val metadata: Map<String, YamlNode>? = null)
