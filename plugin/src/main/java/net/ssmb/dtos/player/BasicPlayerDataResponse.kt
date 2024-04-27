package net.ssmb.dtos.player

import kotlinx.serialization.Serializable

@Serializable
data class BasicPlayerDataResponse(
    val uuid: String,
    val selectedKitId: String,
    val totalGamesPlayed: Int,
    val totalGamesWon: Int,
    val totalPlaytimeSeconds: Long,
    val isBanned: Boolean,
    val levelExperience: Long,
    val rankElo: Int,
    val rankedMatchesPlayed: Int,
    val areFriendRequestsOff: Boolean,
    val canReceiveRandomMessages: Boolean,
    val firstTime: Boolean,
)
