package net.ssmb.dtos.player

import kotlinx.serialization.Serializable

@Serializable data class IsIpBannedRequest(val ip: String, val playerUuid: String?)
