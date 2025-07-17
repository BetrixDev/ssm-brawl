package dev.betrix.superSmashMobsBrawl.minigames.definitions

import dev.betrix.superSmashMobsBrawl.minigames.instances.MinigameInstance
import dev.betrix.superSmashMobsBrawl.minigames.instances.TestingMinigameInstance
import dev.betrix.superSmashMobsBrawl.minigames.minigame
import dev.betrix.superSmashMobsBrawl.models.MinigameTeam

object TestingMinigameDefinition : MinigameDefinition() {
    override val name = "Testing"
    override val id = "testing"

    override val metadata = minigame {
        description = "A simple testing minigame"
        isHidden = false

        whitelistMap("campsite")
    }

    override fun createInstance(teams: List<MinigameTeam>): MinigameInstance {
        return TestingMinigameInstance(this, teams)
    }
}