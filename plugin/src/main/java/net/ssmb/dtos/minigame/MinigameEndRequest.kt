package net.ssmb.dtos.minigame

import kotlinx.serialization.Serializable

@Serializable()
sealed class MinigameEndRequest(
    val gameId: String,
    val mapId: String,
    val minigameId: String,
    val winningUuids: List<String>
) {
    @Serializable()
    sealed class PlayerEntry(
        val uuid: String,
        val stocksLeft: Int,
    ) {
        @Serializable()
        sealed class KitEntry(
            val id: String,
            val startTime: Long,
            val endTime: Long,
            val abilityUsage: List<AbilityUsageEntries>
        ) {
            @Serializable()
            data class AbilityUsageEntries(
                val abilityId: String,
                val usedAt: Long,
                val damageDealt: Double?,
            )
        }
    }
}