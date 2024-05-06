package net.ssmb.dtos.player

import kotlinx.serialization.Serializable

@Serializable
data class UpdatePlayerNameRequest(val playerUuid: String, val username: String)
