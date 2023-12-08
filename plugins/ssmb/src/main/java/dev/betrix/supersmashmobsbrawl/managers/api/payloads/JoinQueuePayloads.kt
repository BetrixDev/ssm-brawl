package dev.betrix.supersmashmobsbrawl.managers.api.payloads

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JoinQueueRequest(val uuid: String, val modeId: String, val isRanked: Boolean)

sealed class JoinQueue {
    data class Error(val value: JoinQueueError) : JoinQueue()
    data class Success(val value: JoinQueueResponse) : JoinQueue()
}

enum class JoinQueueError {
    ALREADY_IN_QUEUE, UNKNOWN
}

@Polymorphic
@Serializable
sealed class JoinQueueResponse {
    @Serializable
    @SerialName("startGame")
    data class StartGame(
        val action: String,
        val playerUuids: List<String>,
        val modeId: String,
        val isRanked: Boolean
    ) : JoinQueueResponse()

    @Serializable
    @SerialName("addedPlayerToQueue")
    data class AddedPlayerToQueue(
        val action: String,
        val playerUuid: String,
        val modeId: String,
        val isRanked: Boolean
    ) : JoinQueueResponse()
}