package dev.betrix.superSmashMobsBrawl.models.brawlData

import dev.betrix.superSmashMobsBrawl.models.SpawnPoint
import kotlinx.serialization.Serializable

@Serializable
data class MapDefFile(
    val gameMaps: List<GameMapDef> = emptyList(),
    val hubMaps: List<HubMapDef> = emptyList(),
)

@Serializable
sealed class MapDef {
    abstract val id: String
    abstract val worldBorderSize: Double
    abstract val voidLevel: Double
    abstract val creators: List<String>
    abstract val spawnPoints: List<SpawnPoint>
}

@Serializable
data class GameMapDef(
    override val id: String,
    override val worldBorderSize: Double,
    override val voidLevel: Double,
    val maxPlayers: Int,
    override val creators: List<String> = emptyList(),
    override val spawnPoints: List<SpawnPoint>,
    val spectatorSpawnPoint: SpawnPoint,
) : MapDef()

@Serializable
data class HubMapDef(
    override val id: String,
    override val worldBorderSize: Double,
    override val voidLevel: Double,
    override val creators: List<String> = emptyList(),
    override val spawnPoints: List<SpawnPoint>,
) : MapDef()
