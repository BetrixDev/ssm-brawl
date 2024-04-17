package net.ssmb.dtos.minigame

import kotlinx.serialization.Serializable

@Serializable()
data class MinigameEndRequest(
    val gameId: String,
    val mapId: String,
    val minigameId: String,
    val winningUuids: List<String>,
    val players: List<PlayerEntry>
) {
    @Serializable()
    data class PlayerEntry(
        val uuid: String,
        val stocksLeft: Int,
        val leftInProgress: Boolean,
        val kits: List<KitEntry>
    ) {
        @Serializable()
        data class KitEntry(
            val id: String,
            val startTime: Long,
            val endTime: Long,
            val abilityUsage: List<AbilityUsageEntry>
        ) {
            @Serializable()
            data class AbilityUsageEntry(
                val abilityId: String,
                val usedAt: Long,
                val damageDealt: Double?,
            )
        }
    }
}
