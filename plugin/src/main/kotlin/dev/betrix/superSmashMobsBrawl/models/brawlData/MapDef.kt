package dev.betrix.superSmashMobsBrawl.models.brawlData

import dev.betrix.superSmashMobsBrawl.models.SpawnPoint
import kotlinx.serialization.Serializable

@Serializable data class MapDefFile(val gameMaps: List<GameMapDef>, val hubMaps: List<HubMapDef>)

@Serializable
sealed class MapDef {
    abstract val id: String
    abstract val worldBorderSize: Double
}

@Serializable
data class GameMapDef(
    override val id: String,
    override val worldBorderSize: Double,
    val voidLevel: Double,
    val maxPlayers: Int,
    val creators: List<String>?,
    val spawnPoints: List<SpawnPoint>,
) : MapDef()

@Serializable
data class HubMapDef(
    override val id: String,
    override val worldBorderSize: Double,
    val voidLevel: Double,
    val creators: List<String>?,
    val spawnPoints: List<SpawnPoint>,
) : MapDef()
