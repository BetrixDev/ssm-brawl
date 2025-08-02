package dev.betrix.superSmashMobsBrawl.minigames.definitions

import dev.betrix.superSmashMobsBrawl.minigames.instances.MinigameInstance
import dev.betrix.superSmashMobsBrawl.minigames.instances.TwoPlayerDuelsMinigameInstance
import dev.betrix.superSmashMobsBrawl.minigames.minigame
import dev.betrix.superSmashMobsBrawl.models.MinigameTeam

object TwoPlayerDuelsMinigameDefinition : MinigameDefinition() {
    override val name = "1v1 Duels"
    override val id = "two_player_duels"

    override val metadata = minigame {
        description = "The most competitive game mode SSMB has to offer"
        isHidden = false
        playersPerTeam = 1
        amountOfTeams = 2
        stocks = 4

        whitelistMap("campsite")
    }

    override fun createInstance(teams: List<MinigameTeam>): MinigameInstance {
        return TwoPlayerDuelsMinigameInstance(this, teams)
    }
}
