package dev.betrix.superSmashMobsBrawl.minigames

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import dev.betrix.superSmashMobsBrawl.models.brawlData.FfaMinigameDef
import org.bukkit.entity.Player

open class FfaMinigame(minigameId: String, gameId: String, players: List<Player>) :
    BrawlMinigame<FfaMinigameDef>(minigameId, gameId, players) {

    override suspend fun initMinigame(): Result<Unit, Exception> {
        return super.initMinigame()
    }

    override fun canPlayerLeaveMinigame(player: Player): Boolean {
        return true
    }

    override fun onPlayerLeave(player: Player) {}
}
