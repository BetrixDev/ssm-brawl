package dev.betrix.superSmashMobsBrawl.minigames

import org.bukkit.entity.Player

class PrototypingMinigame(
    minigameId: String,
    gameId: String,
    players: List<Player>,
) : FfaMinigame(minigameId, gameId, players) {}
