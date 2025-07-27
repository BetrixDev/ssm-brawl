package dev.betrix.superSmashMobsBrawl.minigames.instances

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import dev.betrix.superSmashMobsBrawl.models.MinigameTeam
import java.util.UUID
import org.bukkit.World
import org.bukkit.entity.Player

abstract class MinigameInstance(val definition: MinigameDefinition, val teams: List<MinigameTeam>) {
    open lateinit var world: World
    val gameId: String = UUID.randomUUID().toString()

    open suspend fun setup(): Result<Unit, Exception> {
        return Ok(Unit)
    }

    open suspend fun teardown(): Unit {}

    abstract fun onPlayerLeave(player: Player): Result<Unit, String>

    fun isPlayerInMinigame(player: Player): Boolean {
        return teams.find { it.players.contains(player) } != null
    }
}
