package net.ssmb.dtos.minigame

import kotlinx.serialization.Serializable

@Serializable
data class MinigameStartRequest(val teams: List<List<String>>, val minigameId: String)
