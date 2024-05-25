package net.ssmb.dtos.minigame

import kotlinx.serialization.Serializable
import org.bukkit.entity.Player

data class BukkitTeamData(val teamId: String, val players: ArrayList<Player>)

sealed class MinigameStartResponse {
    data class Success(val value: MinigameStartSuccess) : MinigameStartResponse()

    data class Error(val value: MiniGameError) : MinigameStartResponse()
}

enum class MiniGameError {
    UNKNOWN
}

@Serializable
data class MinigameStartSuccess(
    val gameId: String,
    val minigame: MinigameData,
    val teams: List<TeamsEntry>,
    val map: MapData
) {
    @Serializable
    data class MinigameData(val id: String, val countdownSeconds: Int, val stocks: Int)

    @Serializable data class TeamsEntry(val teamId: String, val players: List<PlayerData>)

    @Serializable
    data class PlayerData(val uuid: String, val selectedKit: KitData) {
        @Serializable
        data class KitData(
            val id: String,
            val meleeDamage: Double,
            val armor: Double,
            val knockbackMult: Double,
            val inventoryIcon: String,
            val helmetId: String?,
            val chestplateId: String?,
            val leggingsId: String?,
            val bootsId: String?,
            val hitboxWidth: Double,
            val hitboxHeight: Double,
            val meta: Map<String, String>?,
            val abilities: List<AbilityEntry>,
            val passives: List<PassiveEntry>,
            val disguise: DisguiseData
        ) {
            @Serializable
            data class AbilityEntry(val ability: AbilityData, val abilityToolSlot: Int) {
                @Serializable
                data class AbilityData(
                    val id: String,
                    val cooldown: Long,
                    val meta: Map<String, String>?
                )
            }

            @Serializable
            data class PassiveEntry(val passive: PassiveData) {
                @Serializable
                data class PassiveData(val id: String, val meta: Map<String, String>?)
            }

            @Serializable
            data class DisguiseData(
                val id: String,
                val displayEntity: String,
                val hurtSound: String
            )
        }
    }

    @Serializable
    data class MapData(val id: String, val spawnPoints: List<Vector3>, val origin: Vector3) {
        @Serializable data class Vector3(val x: Double, val y: Double, val z: Double)
    }
}
