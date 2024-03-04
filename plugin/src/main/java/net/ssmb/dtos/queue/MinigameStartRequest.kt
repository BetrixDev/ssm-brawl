package net.ssmb.dtos.queue

import kotlinx.serialization.Serializable

@Serializable
data class MinigameStartRequest(val playerUuids: List<String>, val minigameId: String)
