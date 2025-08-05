package dev.betrix.superSmashMobsBrawl.models

import kotlinx.serialization.Serializable

@Serializable
data class SpawnPoint(
    val x: Double,
    val y: Double,
    val z: Double,
    val pitch: Double? = null,
    val yaw: Double? = null,
)
