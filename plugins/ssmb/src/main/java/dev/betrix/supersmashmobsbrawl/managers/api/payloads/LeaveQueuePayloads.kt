package dev.betrix.supersmashmobsbrawl.managers.api.payloads

import kotlinx.serialization.Serializable

@Serializable
data class LeaveQueueRequest(val uuid: String)

sealed class LeaveQueue {
    data class Error(val value: LeaveQueueError) : LeaveQueue()
    data class Success(val value: LeaveQueueResponse) : LeaveQueue()
}

enum class LeaveQueueError {
    NOT_IN_QUEUE, UNKNOWN
}

@Serializable
data class LeaveQueueResponse(val modeId: String, val isRanked: Boolean)