package dev.betrix.supersmashmobsbrawl.managers.api.payloads

import kotlinx.serialization.Serializable

@Serializable
data class StartGameRequest(val playerUuids: List<String>, val modeId: String, val isRanked: Boolean)

sealed class StartGame {
    data class Error(val value: StartGameError) : StartGame()
    data class Success(val value: StartGameResponse) : StartGame()
}

enum class StartGameError {
    UNKNOWN
}

@Serializable
data class StartGameResponse(
    val modeId: String,
    val isRanked: Boolean,
    val gameId: String,
    val players: List<PlayerData>,
    val map: MapData
) {
    @Serializable
    data class PlayerData(val uuid: String, val kit: KitData)

    @Serializable
    data class KitData(
        val id: String,
        val inventoryIcon: String,
        val damage: Double,
        val armor: Double,
        val knockback: Double
    )

    @Serializable
    data class MapData(val mapId: String, val displayName: String, val spawnLocations: List<SpawnLocation>)

    @Serializable
    data class SpawnLocation(val x: Int, val y: Int, val z: Int)
}