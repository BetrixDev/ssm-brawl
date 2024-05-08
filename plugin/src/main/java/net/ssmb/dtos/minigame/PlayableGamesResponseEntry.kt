package net.ssmb.dtos.minigame

import kotlinx.serialization.Serializable

@Serializable
class PlayableGamesResponseEntry(
    val id: String,
    val displayName: String,
    val playersInQueue: String
)
