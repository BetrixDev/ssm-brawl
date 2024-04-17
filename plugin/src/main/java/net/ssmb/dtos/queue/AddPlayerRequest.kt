package net.ssmb.dtos.queue

import kotlinx.serialization.Serializable

@Serializable
data class AddPlayerRequest(val playerUuid: String, val minigameId: String, val force: Boolean?)
