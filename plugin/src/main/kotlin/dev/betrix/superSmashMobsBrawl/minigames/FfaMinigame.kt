package dev.betrix.superSmashMobsBrawl.minigames

import dev.betrix.superSmashMobsBrawl.models.MinigamePlayer
import dev.betrix.superSmashMobsBrawl.models.brawlData.FfaMinigameDef
import org.bukkit.entity.Player

open class FfaMinigame(minigameId: String, gameId: String, players: List<MinigamePlayer>) :
    BrawlMinigame<FfaMinigameDef>(minigameId, gameId, players) {

    override fun canPlayerLeaveMinigame(player: Player): Boolean {
        return true
    }

    override fun onPlayerLeave(player: Player) {
        TODO("Implement")
    }
}
