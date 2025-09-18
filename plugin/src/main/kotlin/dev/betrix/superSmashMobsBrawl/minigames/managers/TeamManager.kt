package dev.betrix.superSmashMobsBrawl.minigames.managers

import dev.betrix.superSmashMobsBrawl.IManageable
import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.minigames.BrawlMinigame
import dev.betrix.superSmashMobsBrawl.minigames.MinigameTeam
import org.bukkit.OfflinePlayer

interface ITeamManager : IManageable {
    fun findTeamOf(player: OfflinePlayer): MinigameTeam?

    fun getTeams(): List<MinigameTeam>

    fun onPlayerLeave(player: OfflinePlayer) {}
}

class DefaultTeamManager(private val minigame: BrawlMinigame) : Manageable(), ITeamManager {
    private val playerToTeam =
        mutableMapOf<java.util.UUID, MinigameTeam>()
    private var teams: List<MinigameTeam> = emptyList()

    override fun setup() {
        playerToTeam.clear()
        teams = minigame.teams
        teams.forEach { team -> team.players.forEach { p -> playerToTeam[p.uniqueId] = team } }
    }

    override fun findTeamOf(
        player: OfflinePlayer
    ): MinigameTeam? {
        return playerToTeam[player.uniqueId]
    }

    override fun getTeams(): List<MinigameTeam> = teams
}
