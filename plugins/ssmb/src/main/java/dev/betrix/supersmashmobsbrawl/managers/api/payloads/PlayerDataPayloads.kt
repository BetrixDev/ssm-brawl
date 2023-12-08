package dev.betrix.supersmashmobsbrawl.managers.api.payloads

import kotlinx.serialization.Serializable

@Serializable
data class PlayerDataRequest(
    val uuid: String
)

@Serializable
data class PlayerDataResponse(val uuid: String, val isBanned: Boolean, val selectedKit: String)