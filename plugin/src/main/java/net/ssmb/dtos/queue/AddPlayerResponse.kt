package net.ssmb.dtos.queue

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed class AddPlayerResponse {
    data class Success(val value: AddPlayerSuccess): AddPlayerResponse()
    data class Error(val value: AddPlayerError): AddPlayerResponse()
}

enum class AddPlayerError {
    ALREADY_IN_QUEUE, UNKNOWN
}

@Polymorphic
@Serializable
sealed class AddPlayerSuccess {

    @Serializable
    @SerialName("added")
    data class Added(val action: String, val playersInQueue: Int)

    @Serializable
    @SerialName("start_game")
    sealed class StartGame(
        val action: String,
        val minigameId: String,
        val players: List<PlayerEntry>,
        val map: MapData
    ) {
        @Serializable
        data class PlayerEntry(
            val uuid: String,
            val selectedKitId: String,
            val selectedKid: SelectedKit
        )

        @Serializable
        data class SelectedKit(
            val id: String,
            val meleeDamage: Double,
            val armor: Double,
            val inventoryIcon: String,
            val meta: String
        )

        @Serializable
        sealed class MapData(
            val id: String,
            val minPlayers: Int,
            val maxPlayers: Int,
            val spawnPoints: List<SpawnPoint>
        ) {
            @Serializable
            data class SpawnPoint(
                val mapId: String,
                val x: Double,
                val y: Double,
                val z: Double
            )
        }
    }
}