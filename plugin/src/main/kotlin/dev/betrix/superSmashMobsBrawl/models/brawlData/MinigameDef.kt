package dev.betrix.superSmashMobsBrawl.models.brawlData

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class MinigameDefFile(val minigames: List<MinigameDef>)

@Serializable
sealed class MinigameDef {
    abstract val id: String
    abstract val type: String
    abstract val isHidden: Boolean
    abstract val allowParties: Boolean
    abstract val mapBlacklist: List<String>?
    abstract val mapWhitelist: List<String>?
}

@Serializable
@SerialName("ffa")
data class FfaMinigameDef(
    override val id: String,
    override val type: String,
    override val isHidden: Boolean,
    override val allowParties: Boolean,
    override val mapBlacklist: List<String>? = null,
    override val mapWhitelist: List<String>? = null,
    val minPlayers: Int,
    val maxPlayers: Int,
    val allowKitSwitching: Boolean,
) : MinigameDef()

@Serializable
@SerialName("team_based_stocks")
data class TeamBasedStocksMinigameDef(
    override val id: String,
    override val type: String,
    override val isHidden: Boolean,
    override val allowParties: Boolean,
    override val mapBlacklist: List<String>? = null,
    override val mapWhitelist: List<String>? = null,
    val playersPerTeam: Int,
    val amountOfTeams: Int,
    val stocks: Int,
) : MinigameDef()
