package net.ssmb.dtos.queue

import kotlinx.serialization.Serializable

@Serializable data class RemovePlayerResponse(val playerUuids: List<String>)
