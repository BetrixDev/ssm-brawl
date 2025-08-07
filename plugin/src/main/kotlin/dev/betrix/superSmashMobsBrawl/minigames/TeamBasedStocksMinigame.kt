package dev.betrix.superSmashMobsBrawl.minigames

import dev.betrix.superSmashMobsBrawl.models.BrawlGameWorld
import dev.betrix.superSmashMobsBrawl.models.MinigameTeam
import dev.betrix.superSmashMobsBrawl.models.brawlData.TeamBasedStocksMinigameDef

class TeamBasedStocksMinigame(
    minigameId: String,
    gameId: String,
    brawlWorld: BrawlGameWorld,
    teams: List<MinigameTeam>,
) :
    BrawlMinigame<TeamBasedStocksMinigameDef>(
        minigameId,
        gameId,
        brawlWorld,
        teams.flatMap { it.players },
    ) {

    private val minigameData =
        dataService.getMinigame(minigameId)
            ?: throw RuntimeException("Couldn't find minigame with id $minigameId in DataService")

    override fun setup() {
        super.setup()
    }
}
