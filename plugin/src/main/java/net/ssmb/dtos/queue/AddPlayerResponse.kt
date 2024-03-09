package net.ssmb.dtos.queue

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed class AddPlayerResponse {
    data class Success(val value: AddPlayerSuccess) : AddPlayerResponse()
    data class Error(val value: AddPlayerError) : AddPlayerResponse()
}

enum class AddPlayerError {
    ALREADY_IN_QUEUE, UNKNOWN
}

@Polymorphic
@Serializable
sealed class AddPlayerSuccess {

    @Serializable
    @SerialName("added")
    data class Added(val action: String, val playersInQueue: Int) : AddPlayerSuccess()

    @Serializable
    @SerialName("start_game")
    sealed class StartGame(
        val action: String,
        val minigameId: String,
        val playerUuids: List<String>,
    ) : AddPlayerSuccess()
}