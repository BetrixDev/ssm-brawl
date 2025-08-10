package dev.betrix.superSmashMobsBrawl.models.brawlData

import com.charleskorn.kaml.YamlScalar
import kotlinx.serialization.Serializable

@Serializable data class KitDefFile(val kits: List<KitDef>)

@Serializable
data class KitDef(
    val id: String,
    val meleeDamage: Double,
    val armor: Double,
    val knockbackMultiplier: Double,
    val disguiseId: String? = null,
    val passives: List<KitPassiveDef> = emptyList(),
    val abilities: List<KitAbilityDef> = emptyList(),
    val userFacing: Boolean = true,
)

@Serializable
data class KitPassiveDef(val id: String, val overrides: KitPassiveDefOverrides? = null)

@Serializable data class KitPassiveDefOverrides(val metadata: Map<String, YamlScalar>? = null)

@Serializable
data class KitAbilityDef(val id: String, val overrides: KitAbilityDefOverrides? = null)

@Serializable data class KitAbilityDefOverrides(val metadata: Map<String, YamlScalar>? = null)
