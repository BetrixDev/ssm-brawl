package dev.betrix.superSmashMobsBrawl.models.brawlData

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable data class PassiveFileDef(val passives: List<PassiveDef>)

@Serializable
data class PassiveDef(
    val id: String,
    val userFacing: Boolean,
    val metadata: Map<String, JsonElement>? = null,
    val displayItem: String,
)
