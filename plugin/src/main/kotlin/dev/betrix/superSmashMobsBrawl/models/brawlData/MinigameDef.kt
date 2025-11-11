package dev.betrix.superSmashMobsBrawl.models.brawlData

import com.charleskorn.kaml.YamlScalar
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class KitSwitchingMode {
    NEVER,
    ON_DEATH,
    IMMEDIATE,
    MYSTERY,
}

@Serializable data class MinigameDefFile(val minigames: List<MinigameDef>)

@Serializable
sealed class MinigameDef {
    abstract val id: String
    abstract val isHidden: Boolean
    abstract val allowParties: Boolean
    abstract val mapBlacklist: List<String>?
    abstract val mapWhitelist: List<String>?
    abstract val passiveBlacklist: List<String>?
    abstract val passiveWhitelist: List<String>?
    abstract val respawnDelaySeconds: Int?
    abstract val overrides: MinigameDefOverrides?
    abstract val allowRejoinAfterLeave: Boolean
    abstract val kitSwitchingMode: KitSwitchingMode

    fun isPassiveValid(passiveId: String): Boolean {
        if (passiveBlacklist?.contains(passiveId) == true) {
            return false
        }

        if (passiveWhitelist != null) {
            return passiveWhitelist?.contains(passiveId) == true
        }

        return true
    }
}

@Serializable
@SerialName("ffa")
data class FfaMinigameDef(
    override val id: String,
    override val isHidden: Boolean,
    override val allowParties: Boolean,
    override val mapBlacklist: List<String>? = null,
    override val mapWhitelist: List<String>? = null,
    override val passiveBlacklist: List<String>? = null,
    override val passiveWhitelist: List<String>? = null,
    override val respawnDelaySeconds: Int? = null,
    override val overrides: MinigameDefOverrides? = null,
    override val allowRejoinAfterLeave: Boolean = true,
    override val kitSwitchingMode: KitSwitchingMode = KitSwitchingMode.NEVER,
    val minPlayers: Int,
    val maxPlayers: Int,
) : MinigameDef()

@Serializable
@SerialName("team_based_stocks")
data class TeamBasedStocksMinigameDef(
    override val id: String,
    override val isHidden: Boolean,
    override val allowParties: Boolean,
    override val mapBlacklist: List<String>? = null,
    override val mapWhitelist: List<String>? = null,
    override val passiveBlacklist: List<String>? = null,
    override val passiveWhitelist: List<String>? = null,
    override val respawnDelaySeconds: Int? = null,
    override val overrides: MinigameDefOverrides? = null,
    override val allowRejoinAfterLeave: Boolean = true,
    override val kitSwitchingMode: KitSwitchingMode = KitSwitchingMode.NEVER,
    val playersPerTeam: Int,
    val amountOfTeams: Int,
    val stocks: Int,
) : MinigameDef()

@Serializable
data class MinigameDefOverrides(
    val kits: Map<String, MinigameDefKitOverrides>? = null,
    val passives: Map<String, KitPassiveDefOverrides>? = null,
    val abilities: Map<String, KitAbilityDefOverrides>? = null,
)

@Serializable
data class MinigameDefKitOverrides(
    val metadata: Map<String, YamlScalar>? = null,
    val passives: Map<String, KitPassiveDefOverrides>? = null,
    val abilities: Map<String, KitAbilityDefOverrides>? = null,
)
