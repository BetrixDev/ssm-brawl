package dev.betrix.superSmashMobsBrawl.minigames

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import dev.betrix.superSmashMobsBrawl.extensions.getEquidistant
import dev.betrix.superSmashMobsBrawl.extensions.location
import dev.betrix.superSmashMobsBrawl.models.BrawlGameWorld
import dev.betrix.superSmashMobsBrawl.models.brawlData.FfaMinigameDef
import org.bukkit.entity.Player

class FfaMinigame(
    minigameId: String,
    gameId: String,
    brawlWorld: BrawlGameWorld,
    players: List<Player>,
) : BrawlMinigame<FfaMinigameDef>(minigameId, gameId, brawlWorld, players) {

    override suspend fun initMinigame(): Result<Unit, Exception> {
        val spawnPoints = brawlWorld.data.spawnPoints.getEquidistant(players.size)

        players.forEachIndexed { idx, player ->
            player.teleport(brawlWorld.world.location(spawnPoints[idx]))
        }

        return Ok(Unit)
    }
}
