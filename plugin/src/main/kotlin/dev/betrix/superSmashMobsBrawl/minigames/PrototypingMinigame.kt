package dev.betrix.superSmashMobsBrawl.minigames

import dev.betrix.superSmashMobsBrawl.models.BrawlGameWorld
import org.bukkit.entity.Player

class PrototypingMinigame(
    minigameId: String,
    gameId: String,
    brawlWorld: BrawlGameWorld,
    players: List<Player>,
) : FfaMinigame(minigameId, gameId, brawlWorld, players) {}
