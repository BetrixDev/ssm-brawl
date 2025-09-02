package dev.betrix.superSmashMobsBrawl.systems.scoreboards

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import dev.betrix.superSmashMobsBrawl.components.InMinigameComponent
import dev.betrix.superSmashMobsBrawl.components.PlayerComponent
import dev.betrix.superSmashMobsBrawl.components.ScoreboardComponent
import dev.betrix.superSmashMobsBrawl.services.LangService
import net.kyori.adventure.text.Component

class MinigameScoreboardSystem(private val lang: LangService = inject()) :
    IteratingSystem(family { all(PlayerComponent, ScoreboardComponent, InMinigameComponent) }) {
    override fun onTickEntity(entity: Entity) {
        val scoreboardComponent = entity[ScoreboardComponent]
        val player = entity[PlayerComponent].player
        val minigameComponent = entity[InMinigameComponent]

        // Set header with minigame info
        val headerLines =
            listOf(
                lang.t("scoreboards.common.title") // Could be minigame-specific
                // Add minigame name or status here
            )
        scoreboardComponent.setHeaderLines(headerLines)

        // Set content with game-specific stats
        val contentLines =
            listOf<Component>(
                // TODO: Add minigame-specific content like:
                // - Player stats (kills, deaths, etc.)
                // - Game timer
                // - Team information
                // - Objective status
            )
        scoreboardComponent.setContentLines(contentLines)

        // Update with common divider
        val divider = lang.t("scoreboards.common.divider")
        scoreboardComponent.updateScoreboard(divider)
    }
}
