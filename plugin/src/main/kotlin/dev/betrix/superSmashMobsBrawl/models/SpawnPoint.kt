package dev.betrix.superSmashMobsBrawl.models

import kotlinx.serialization.Serializable

@Serializable
data class SpawnPoint(
    val x: Double,
    val y: Double,
    val z: Double,
    val pitch: Float = 0f,
    val yaw: Float = 0f,
)
