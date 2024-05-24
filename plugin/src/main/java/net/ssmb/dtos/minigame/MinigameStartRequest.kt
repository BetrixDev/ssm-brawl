package net.ssmb.dtos.minigame

import kotlinx.serialization.Serializable

@Serializable
data class MinigameStartRequest(val teams: List<TeamEntry>, val minigameId: String) {
    @Serializable
    data class TeamEntry(val id: String, val players: List<String>)
}
