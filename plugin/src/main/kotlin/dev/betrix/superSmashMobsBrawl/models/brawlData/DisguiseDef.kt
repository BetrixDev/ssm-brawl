package dev.betrix.superSmashMobsBrawl.models.brawlData

import kotlinx.serialization.Serializable

@Serializable data class DisguiseDefFile(val disguises: List<DisguiseDef>)

@Serializable data class DisguiseDef(val id: String)
