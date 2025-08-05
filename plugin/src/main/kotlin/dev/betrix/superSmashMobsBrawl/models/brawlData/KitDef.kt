package dev.betrix.superSmashMobsBrawl.models.brawlData

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable data class KitDefFile(val kits: List<KitDef>)

@Serializable
data class KitDef(
    val id: String,
    val meleeDamage: Double,
    val armor: Double,
    val knockbackMultiplier: Double,
    val passives: KitPassiveDef,
    val abilities: KitAbilityDef,
)

@Serializable data class KitPassiveDef(val id: String, val overrides: KitPassiveDefOverride?)

@Serializable data class KitPassiveDefOverride(val metadata: Map<String, JsonElement>?)

@Serializable data class KitAbilityDef(val id: String, val overrides: KitAbilityDefOverride?)

@Serializable data class KitAbilityDefOverride(val metadata: Map<String, JsonElement>?)
