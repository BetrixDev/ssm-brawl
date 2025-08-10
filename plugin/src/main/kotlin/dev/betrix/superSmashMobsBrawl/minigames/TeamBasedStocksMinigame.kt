package dev.betrix.superSmashMobsBrawl.minigames

import dev.betrix.superSmashMobsBrawl.models.MinigameTeam
import dev.betrix.superSmashMobsBrawl.models.brawlData.TeamBasedStocksMinigameDef
import org.bukkit.entity.Player

open class TeamBasedStocksMinigame(minigameId: String, gameId: String, teams: List<MinigameTeam>) :
    BrawlMinigame<TeamBasedStocksMinigameDef>(minigameId, gameId, teams.flatMap { it.players }) {

    override fun canPlayerLeaveMinigame(player: Player): Boolean {
        return true
    }

    override fun onPlayerLeave(player: Player) {
        TODO("Implement")
    }
}
