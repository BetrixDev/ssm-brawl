package dev.betrix.superSmashMobsBrawl.systems.scoreboards

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import dev.betrix.superSmashMobsBrawl.components.InHubComponent
import dev.betrix.superSmashMobsBrawl.components.PlayerComponent
import dev.betrix.superSmashMobsBrawl.components.ScoreboardComponent
import dev.betrix.superSmashMobsBrawl.services.LangService

class HubScoreboardSystem(
    private val lang: LangService = inject()
) : IteratingSystem(
    family { all(PlayerComponent, ScoreboardComponent, InHubComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val scoreboardComponent = entity[ScoreboardComponent]
        val player = entity[PlayerComponent].player

        // Example: Hub-specific header with welcome message
        val headerLines = listOf(
            lang.t("scoreboards.hub.content.player_info") { "player" to player.name }
        )
        scoreboardComponent.setHeaderLines(headerLines)

        // The hub system only overrides the header section,
        // allowing other systems (like the base ScoreboardSystem) to handle
        // the content and footer sections

        // Update with common divider
        val divider = lang.t("scoreboards.common.divider")
        scoreboardComponent.updateScoreboard(divider)
    }
}