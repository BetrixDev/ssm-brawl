package dev.betrix.superSmashMobsBrawl.models.brawlData

import com.charleskorn.kaml.YamlScalar
import dev.betrix.superSmashMobsBrawl.serializers.MaterialSerializer
import dev.betrix.superSmashMobsBrawl.serializers.SoundSerializer
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.Sound

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
    val armorItems: KitArmorItemsDef? = null,
    val meleeReach: Double = 3.0,
    @Serializable(with = MaterialSerializer::class) val displayItem: Material? = null,
    @Serializable(with = SoundSerializer::class) val selectionSound: Sound? = null,
    val userFacing: Boolean = true,
)

@Serializable
data class KitPassiveDef(val id: String, val overrides: KitPassiveDefOverrides? = null)

@Serializable data class KitPassiveDefOverrides(val metadata: Map<String, YamlScalar>? = null)

@Serializable
data class KitAbilityDef(val id: String, val overrides: KitAbilityDefOverrides? = null)

@Serializable data class KitAbilityDefOverrides(val metadata: Map<String, YamlScalar>? = null)

@Serializable
data class KitArmorItemsDef(
    val helmet: String? = null,
    val chestplate: String? = null,
    val leggings: String? = null,
    val boots: String? = null,
)
