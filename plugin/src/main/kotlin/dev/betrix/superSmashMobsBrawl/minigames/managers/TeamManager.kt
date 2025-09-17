package dev.betrix.superSmashMobsBrawl.minigames.managers

import dev.betrix.superSmashMobsBrawl.IManageable
import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.minigames.BrawlMinigame
import org.bukkit.OfflinePlayer

interface ITeamManager : IManageable {
    fun findTeamOf(player: OfflinePlayer): dev.betrix.superSmashMobsBrawl.minigames.MinigameTeam?

    fun getTeams(): List<dev.betrix.superSmashMobsBrawl.minigames.MinigameTeam>

    fun onPlayerLeave(player: OfflinePlayer) {}
}

class DefaultTeamManager(private val minigame: BrawlMinigame) : Manageable(), ITeamManager {
    private val playerToTeam =
        mutableMapOf<java.util.UUID, dev.betrix.superSmashMobsBrawl.minigames.MinigameTeam>()
    private var teams: List<dev.betrix.superSmashMobsBrawl.minigames.MinigameTeam> = emptyList()

    override fun setup() {
        playerToTeam.clear()
        teams = minigame.teams
        teams.forEach { team -> team.players.forEach { p -> playerToTeam[p.uniqueId] = team } }
    }

    override fun findTeamOf(
        player: OfflinePlayer
    ): dev.betrix.superSmashMobsBrawl.minigames.MinigameTeam? {
        return playerToTeam[player.uniqueId]
    }

    override fun getTeams(): List<dev.betrix.superSmashMobsBrawl.minigames.MinigameTeam> = teams
}
