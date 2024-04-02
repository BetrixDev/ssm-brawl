package net.ssmb.dtos.minigame

import kotlinx.serialization.Serializable

sealed class MinigameStartResponse {
    data class Success(val value: MinigameStartSuccess) : MinigameStartResponse()
    data class Error(val value: MiniGameError) : MinigameStartResponse()
}

enum class MiniGameError {
    UNKNOWN
}

@Serializable
sealed class MinigameStartSuccess(
    val gameId: String,
    val minigame: MinigameData,
    val players: List<PlayerData>,
    val map: MapData
) {
    @Serializable
    data class MinigameData(val id: String, val countdownSeconds: Int, val stocks: Int)

    @Serializable
    sealed class PlayerData(val uuid: String, val selectedKit: KitData) {
        @Serializable
        sealed class KitData(
            val id: String,
            val meleeDamage: Double,
            val armor: Double,
            val knockbackMult: Double,
            val inventoryIcon: String,
            val meta: Map<String, String>?,
            val abilities: List<AbilityEntry>,
            val passives: List<PassiveEntry>,
            val helmetId: String?,
            val chestplateId: String?,
            val leggingsId: String?,
            val bootsId: String?,
            val hitboxWidth: Double,
            val hitboxHeight: Double,
            val disguise: DisguiseData
        ) {
            @Serializable
            sealed class AbilityEntry(val ability: AbilityData, val abilityToolSlot: Int) {
                @Serializable
                data class AbilityData(val id: String, val cooldown: Long, val meta: Map<String, String>?)
            }

            @Serializable
            sealed class PassiveEntry(val passive: PassiveData) {
                @Serializable
                data class PassiveData(val id: String, val meta: Map<String, String>?)
            }

            @Serializable
            data class DisguiseData(val id: String, val displayEntity: String, val hurtSound: String)
        }
    }

    @Serializable
    sealed class MapData(val id: String, val spawnPoints: List<SpawnPoint>) {
        @Serializable
        data class SpawnPoint(val x: Double, val y: Double, val z: Double)
    }
}