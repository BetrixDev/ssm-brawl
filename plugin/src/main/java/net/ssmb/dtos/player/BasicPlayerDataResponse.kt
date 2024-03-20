package net.ssmb.dtos.player

import kotlinx.serialization.Serializable

@Serializable
data class BasicPlayerDataResponse(
    val uuid: String,
    val selectedKidId: String,
    val totalGamesPlayed: Int,
    val totalGamesWon: Int,
    val totalPlaytimeSeconds: Long,
    val firstTime: Boolean,
    val isBanned: Boolean
)