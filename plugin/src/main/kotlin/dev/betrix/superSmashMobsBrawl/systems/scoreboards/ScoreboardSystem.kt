package dev.betrix.superSmashMobsBrawl.systems.scoreboards

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.Ticker
import dev.betrix.superSmashMobsBrawl.components.PlayerComponent
import dev.betrix.superSmashMobsBrawl.components.ScoreboardComponent
import dev.betrix.superSmashMobsBrawl.services.LangService

class ScoreboardSystem(
    val lang: LangService = inject(),
    val plugin: SuperSmashMobsBrawl = inject(),
) : IteratingSystem(family { all(ScoreboardComponent) }) {
    private val ticker = Ticker(120)

    override fun onTickEntity(entity: Entity) {
        val scoreboardComponent = entity[ScoreboardComponent]
        val player = entity[PlayerComponent].player

        val onlinePlayers = plugin.server.onlinePlayers.size
        val maxPlayers = plugin.server.maxPlayers

        // Set scoreboard title with animation
        scoreboardComponent.setTitle(
            lang.t("scoreboards.common.title") { "percent" to ticker.nextPercent().toString() }
        )

        // Set content section with player info
        val contentLines =
            listOf(
                lang.t("scoreboards.hub.content.player_info") { "player" to player.name },
                lang.t("scoreboards.hub.content.online_count") {
                    "current" to onlinePlayers.toString()
                    "max" to maxPlayers.toString()
                },
            )
        scoreboardComponent.setContentLines(contentLines)

        // Set tab header and footer with placeholders
        scoreboardComponent.setTabHeader(
            lang
                .t("scoreboards.common.tab.header")
                .appendNewline()
                .append(lang.t("scoreboards.hub.tab.header") { "player" to player.name })
        )

        scoreboardComponent.setTabFooter(
            lang
                .t("scoreboards.hub.tab.footer") {
                    "current" to onlinePlayers.toString()
                    "max" to maxPlayers.toString()
                }
                .appendNewline()
                .append(lang.t("scoreboards.common.tab.footer"))
        )

        // Get divider and update scoreboard
        val divider = lang.t("scoreboards.common.divider")
        scoreboardComponent.updateScoreboard(divider)
    }
}
