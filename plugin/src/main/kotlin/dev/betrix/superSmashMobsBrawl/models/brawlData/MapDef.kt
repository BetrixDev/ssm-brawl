package dev.betrix.superSmashMobsBrawl.models.brawlData

import dev.betrix.superSmashMobsBrawl.models.SpawnPoint
import kotlinx.serialization.Serializable

@Serializable data class MapDefFile(val gameMaps: List<GameMapDef>, val hubMaps: List<HubMapDef>)

@Serializable
data class GameMapDef(
    val id: String,
    val voidLevel: Double,
    val maxPlayers: Int,
    val worldBorderSize: Double,
    val creators: List<String>,
    val spawnPoints: List<SpawnPoint>,
)

@Serializable
data class HubMapDef(
    val id: String,
    val voidLevel: Double,
    val worldBorderSize: Double,
    val creators: List<String>,
    val spawnPoints: List<SpawnPoint>,
)
