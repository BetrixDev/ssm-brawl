package dev.betrix.superSmashMobsBrawl.minigames.components.scoreboard.impl

import dev.betrix.superSmashMobsBrawl.minigames.components.scoreboard.MinigameScoreboardManager
import dev.betrix.superSmashMobsBrawl.models.MinigameState
import gg.flyte.twilight.scoreboard.TwilightScoreboard
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

class DefaultScoreboardManager : MinigameScoreboardManager() {
    private val scoreboards = mutableMapOf<Player, TwilightScoreboard>()

    override fun onTick() {
        scoreboards.forEach { _, scoreboard ->
            scoreboard.apply {
                updateSidebarTitle(Component.text("SSM BRAWL"))
            }
        }

        when (minigame.state) {
            MinigameState.STARTING -> {}
            MinigameState.ONGOING -> {}
            MinigameState.ENDED -> {}
            else -> {}
        }
    }

    override fun onPlayerJoin(player: Player) {
        scoreboards[player] = TwilightScoreboard(player)
    }

    override fun onPlayerLeave(player: Player) {
        scoreboards[player]?.delete()
        scoreboards.remove(player)
    }

    override fun teardown() {
        scoreboards.forEach { it.value.delete() }
        scoreboards.clear()
    }
}