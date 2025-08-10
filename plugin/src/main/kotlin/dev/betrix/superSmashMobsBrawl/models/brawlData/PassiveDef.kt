package dev.betrix.superSmashMobsBrawl.models.brawlData

import com.charleskorn.kaml.YamlScalar
import kotlinx.serialization.Serializable

@Serializable data class PassiveFileDef(val passives: List<PassiveDef>)

@Serializable
data class PassiveDef(
    val id: String,
    val userFacing: Boolean,
    val metadata: Map<String, YamlScalar>? = null,
    val displayItem: String? = null,
)
