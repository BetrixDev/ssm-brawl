package dev.betrix.superSmashMobsBrawl.minigames.components.scoreboard

import dev.betrix.superSmashMobsBrawl.minigames.components.MinigameComponent
import org.bukkit.entity.Player

abstract class MinigameScoreboardManager : MinigameComponent() {
    abstract fun onTick()
    abstract fun onPlayerJoin(player: Player)
    abstract fun onPlayerLeave(player: Player)
}