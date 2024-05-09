package net.ssmb.dtos.maps

import kotlinx.serialization.Serializable

@Serializable
data class GetMapDetailsResponse(
    val id: String,
    val minPlayers: Int,
    val maxPlayers: Int,
    val worldBorderRadius: Int,
    val role: String,
    val origin: Vector3,
    val spawnPoints: List<Vector3>
) {
    @Serializable
    data class Vector3(val x: Double, val y: Double, val z: Double)
}
