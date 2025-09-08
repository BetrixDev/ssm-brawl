package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.collection.MutableEntityBag
import com.github.quillraven.fleks.collection.mutableEntityBagOf
import dev.betrix.superSmashMobsBrawl.models.BrawlWorld
import dev.betrix.superSmashMobsBrawl.models.TeamData
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import java.util.UUID

enum class MinigameState {
    LOADING_WORLD, // World is being loaded
    WAITING, // Waiting for players
    STARTING, // Countdown before game starts
    ONGOING, // Game is active
    ENDING, // Game finished, showing results
    CLEANUP, // Cleaning up resources
    PAUSED, // Game is paused, cooldowns will stop
}

/**
 * Base component for all minigames containing common data and state. This component should be
 * present on every minigame entity.
 */
data class MinigameComponent(
    val minigame: MinigameDef,
    val instanceId: String = generateInstanceId(),
    var state: MinigameState = MinigameState.LOADING_WORLD,
    val playerEntities: MutableEntityBag,
    val spectatorEntities: MutableEntityBag = mutableEntityBagOf(),
    val disconnectedPlayers: MutableSet<UUID> = mutableSetOf(),
) : Component<MinigameComponent> {
    override fun type() = MinigameComponent

    companion object : ComponentType<MinigameComponent>()

    lateinit var loadedWorld: BrawlWorld

    fun hasLoadedWorld(): Boolean = ::loadedWorld.isInitialized
}

/** Component for team-based minigames. Contains team data and management functionality. */
data class TeamMinigameComponent(val teams: MutableMap<String, TeamData> = mutableMapOf()) :
    Component<TeamMinigameComponent> {
    override fun type() = TeamMinigameComponent

    companion object : ComponentType<TeamMinigameComponent>()

    fun getTeam(teamId: String): TeamData? = teams[teamId]

    fun addTeam(teamData: TeamData) {
        teams[teamData.teamId] = teamData
    }

    fun removeTeam(teamId: String) {
        teams.remove(teamId)
    }

    fun getAllTeams(): List<TeamData> = teams.values.toList()
}

/** Component for stock-based minigames where players/teams have limited lives. */
data class StocksMinigameComponent(
    var defaultStocks: Int = 3,
    val playerStocks: MutableMap<String, Int> = mutableMapOf(), // player entity ID -> stocks
    val teamStocks: MutableMap<String, Int> = mutableMapOf(), // team ID -> stocks
) : Component<StocksMinigameComponent> {
    override fun type() = StocksMinigameComponent

    companion object : ComponentType<StocksMinigameComponent>()

    fun getPlayerStocks(playerId: String): Int = playerStocks[playerId] ?: defaultStocks

    fun usePlayerStock(playerId: String): Boolean {
        val currentStocks = getPlayerStocks(playerId)
        return if (currentStocks > 0) {
            playerStocks[playerId] = currentStocks - 1
            true
        } else {
            false
        }
    }

    fun getTeamStocks(teamId: String): Int = teamStocks[teamId] ?: defaultStocks

    fun useTeamStock(teamId: String): Boolean {
        val currentStocks = getTeamStocks(teamId)
        return if (currentStocks > 0) {
            teamStocks[teamId] = currentStocks - 1
            true
        } else {
            false
        }
    }
}

/** Component for combat-focused minigames tracking kills, deaths, and damage. */
data class CombatMinigameComponent(
    val playerKills: MutableMap<String, Int> = mutableMapOf(), // player entity ID -> kill count
    val playerDeaths: MutableMap<String, Int> = mutableMapOf(), // player entity ID -> death count
    val playerDamage: MutableMap<String, Double> =
        mutableMapOf(), // player entity ID -> damage dealt
    val teamKills: MutableMap<String, Int> = mutableMapOf(), // team ID -> total kills
) : Component<CombatMinigameComponent> {
    override fun type() = CombatMinigameComponent

    companion object : ComponentType<CombatMinigameComponent>()

    fun addKill(playerId: String, teamId: String? = null) {
        playerKills[playerId] = (playerKills[playerId] ?: 0) + 1
        teamId?.let { teamKills[it] = (teamKills[it] ?: 0) + 1 }
    }

    fun addDeath(playerId: String) {
        playerDeaths[playerId] = (playerDeaths[playerId] ?: 0) + 1
    }

    fun addDamage(playerId: String, damage: Double) {
        playerDamage[playerId] = (playerDamage[playerId] ?: 0.0) + damage
    }

    fun getKills(playerId: String): Int = playerKills[playerId] ?: 0

    fun getDeaths(playerId: String): Int = playerDeaths[playerId] ?: 0

    fun getDamage(playerId: String): Double = playerDamage[playerId] ?: 0.0

    fun getTeamKills(teamId: String): Int = teamKills[teamId] ?: 0
}

/** Component for time-based minigames with countdowns and match durations. */
data class TimedMinigameComponent(
    var countdownSeconds: Int = 0,
    var matchDurationSeconds: Int? = null, // null = unlimited time
    var startTime: Long? = null,
    var endTime: Long? = null,
    var isPaused: Boolean = false,
    var pausedAt: Long? = null,
    var totalPausedTime: Long = 0,
) : Component<TimedMinigameComponent> {
    override fun type() = TimedMinigameComponent

    companion object : ComponentType<TimedMinigameComponent>()

    fun getRemainingTime(): Long? {
        val duration = matchDurationSeconds ?: return null
        val start = startTime ?: return null
        val elapsed = System.currentTimeMillis() - start - totalPausedTime
        return (duration * 1000) - elapsed
    }

    fun getElapsedTime(): Long {
        val start = startTime ?: return 0
        return System.currentTimeMillis() - start - totalPausedTime
    }

    fun startMatch() {
        startTime = System.currentTimeMillis()
    }

    fun endMatch() {
        endTime = System.currentTimeMillis()
    }

    fun pause() {
        if (!isPaused) {
            isPaused = true
            pausedAt = System.currentTimeMillis()
        }
    }

    fun resume() {
        if (isPaused && pausedAt != null) {
            totalPausedTime += System.currentTimeMillis() - pausedAt!!
            isPaused = false
            pausedAt = null
        }
    }
}

/** Component for parkour-based minigames tracking checkpoints and completion times. */
data class ParkourMinigameComponent(
    val checkpoints: List<ParkourCheckpoint> = listOf(),
    val playerCheckpoints: MutableMap<String, MutableSet<Int>> =
        mutableMapOf(), // player ID -> completed checkpoint indices
    val playerCompletionTimes: MutableMap<String, Long> =
        mutableMapOf(), // player ID -> completion time in milliseconds
    val playerBestLaps: MutableMap<String, Long> = mutableMapOf(), // player ID -> best lap time
    val leaderboard: MutableList<ParkourLeaderboardEntry> = mutableListOf(),
) : Component<ParkourMinigameComponent> {
    override fun type() = ParkourMinigameComponent

    companion object : ComponentType<ParkourMinigameComponent>()

    fun completeCheckpoint(playerId: String, checkpointIndex: Int) {
        playerCheckpoints.computeIfAbsent(playerId) { mutableSetOf() }.add(checkpointIndex)
    }

    fun hasCompletedCheckpoint(playerId: String, checkpointIndex: Int): Boolean {
        return playerCheckpoints[playerId]?.contains(checkpointIndex) ?: false
    }

    fun getCompletedCheckpoints(playerId: String): Set<Int> {
        return playerCheckpoints[playerId]?.toSet() ?: emptySet()
    }

    fun recordCompletion(playerId: String, completionTime: Long) {
        playerCompletionTimes[playerId] = completionTime
        updateLeaderboard(playerId, completionTime)
    }

    private fun updateLeaderboard(playerId: String, time: Long) {
        leaderboard.removeIf { it.playerId == playerId }
        leaderboard.add(ParkourLeaderboardEntry(playerId, time))
        leaderboard.sortBy { it.completionTime }
    }
}

data class ParkourCheckpoint(
    val index: Int,
    val name: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val radius: Double = 2.0,
)

data class ParkourLeaderboardEntry(val playerId: String, val completionTime: Long)

private fun generateInstanceId(): String {
    return "minigame_${UUID.randomUUID()}"
}
