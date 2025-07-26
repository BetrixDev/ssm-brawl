package dev.betrix.superSmashMobsBrawl.minigames.instances

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import dev.betrix.superSmashMobsBrawl.models.MinigameTeam
import org.bukkit.World
import org.bukkit.entity.Player

abstract class MinigameInstance(val definition: MinigameDefinition, val teams: List<MinigameTeam>) {
    open lateinit var world: World

    open fun setup(): Result<Unit, Exception> {
        return Ok(Unit)
    }

    open fun teardown(): Unit {}

    abstract fun onPlayerLeave(player: Player): Result<Unit, String>

    fun isPlayerInMinigame(player: Player): Boolean {
        return teams.find { it.players.contains(player) } != null
    }
}
