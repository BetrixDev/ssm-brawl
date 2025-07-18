package dev.betrix.superSmashMobsBrawl.minigames.instances

import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import dev.betrix.superSmashMobsBrawl.models.MinigameTeam
import org.bukkit.World
import org.bukkit.entity.Player

abstract class MinigameInstance(
    val definition: MinigameDefinition,
    val teams: List<MinigameTeam>
) {
    lateinit var world: World

    /**
     * This method returns a Result type so that whatever is calling the setup method will be forced to handle any possible
     * error state and make sure the players and possible world created are dealt with accordingly.
     */
    open fun setup(): Result<Unit> {
        return Result.success(Unit)
    }

    open fun teardown(): Unit {}

    abstract fun onPlayerLeave(player: Player): Unit

    fun isPlayerInMinigame(player: Player): Boolean {
        return teams.find { it.teams.contains(player) } != null
    }
}