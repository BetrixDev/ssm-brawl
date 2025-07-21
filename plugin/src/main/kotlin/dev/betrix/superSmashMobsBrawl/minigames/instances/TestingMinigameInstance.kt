package dev.betrix.superSmashMobsBrawl.minigames.instances

import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import dev.betrix.superSmashMobsBrawl.models.MinigameTeam
import org.bukkit.entity.Player

class TestingMinigameInstance(
    definition: MinigameDefinition,
    teams: List<MinigameTeam>
) : MinigameInstance(definition, teams) {
    override fun setup(): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun teardown() {
        TODO("Not yet implemented")
    }

    override fun onPlayerLeave(player: Player) {
        TODO("Not yet implemented")
    }
}