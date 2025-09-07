package dev.betrix.superSmashMobsBrawl.systems.scoreboards

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.Ticker
import dev.betrix.superSmashMobsBrawl.components.InMinigameComponent
import dev.betrix.superSmashMobsBrawl.components.InQueueComponent
import dev.betrix.superSmashMobsBrawl.components.PlayerComponent
import dev.betrix.superSmashMobsBrawl.components.ScoreboardComponent
import dev.betrix.superSmashMobsBrawl.models.brawlData.FfaMinigameDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.TeamBasedStocksMinigameDef
import dev.betrix.superSmashMobsBrawl.services.LangService

class QueueScoreboardSystem(
    private val lang: LangService = inject(),
    private val plugin: SuperSmashMobsBrawl = inject(),
) : IteratingSystem(family { all(PlayerComponent, ScoreboardComponent, InQueueComponent) }) {
    private val ticker = Ticker(120)

    override fun onTickEntity(entity: Entity) {
        val scoreboardComponent = entity[ScoreboardComponent]
        val player = entity[PlayerComponent].player
        val inQueueComponent = entity[InQueueComponent]

        val minigameId = inQueueComponent.minigame.id
        val requiredPlayers = getRequiredPlayersForMinigame(inQueueComponent.minigame)
        val playersInQueue = getPlayersInQueue(minigameId)
        val queueTime = System.currentTimeMillis() - inQueueComponent.startTimestamp
        val queueTimeSeconds = queueTime / 1000

        // Set scoreboard title with animation
        scoreboardComponent.setTitle(
            lang.t("scoreboards.common.title") { "percent" to ticker.nextPercent().toString() }
        )

        // Set header section
        val headerLines =
            listOf(
                lang.t("scoreboards.queue.header.in_queue"),
                lang.t("minigames.${minigameId}.name"),
            )
        scoreboardComponent.setHeaderLines(headerLines)

        // Set content section with placeholders
        val contentLines =
            listOf(
                lang.t("scoreboards.queue.content.players_label"),
                lang.t("scoreboards.queue.content.players_count") {
                    "current" to playersInQueue.toString()
                    "required" to requiredPlayers.toString()
                },
                lang.t("scoreboards.queue.content.time_label"),
                lang.t("scoreboards.queue.content.time_value") {
                    "seconds" to queueTimeSeconds.toString()
                },
            )
        scoreboardComponent.setContentLines(contentLines)

        // Set tab header and footer with placeholders
        scoreboardComponent.setTabHeader(
            lang
                .t("scoreboards.common.tab.header")
                .appendNewline()
                .append(lang.t("scoreboards.queue.tab.header") { "minigameId" to minigameId })
        )

        scoreboardComponent.setTabFooter(
            lang
                .t("scoreboards.queue.tab.footer") {
                    "current" to playersInQueue.toString()
                    "required" to requiredPlayers.toString()
                    "seconds" to queueTimeSeconds.toString()
                }
                .appendNewline()
                .append(lang.t("scoreboards.common.tab.footer"))
        )

        // Get divider and update scoreboard
        val divider = lang.t("scoreboards.common.divider")
        scoreboardComponent.updateScoreboard(divider)
    }

    private fun getRequiredPlayersForMinigame(minigameDef: MinigameDef): Int {
        return when (minigameDef) {
            is TeamBasedStocksMinigameDef -> {
                minigameDef.playersPerTeam * minigameDef.amountOfTeams
            }

            is FfaMinigameDef -> {
                minigameDef.minPlayers
            }
        }
    }

    private fun getPlayersInQueue(minigameId: String): Int {
        var count = 0

        // Count all players with QueueComponent for the same minigame
        // who are not already in a minigame
        world
            .family { all(PlayerComponent, InQueueComponent) }
            .forEach { entity ->
                if (!entity.has(InMinigameComponent)) {
                    val inQueueComponent = entity[InQueueComponent]
                    if (inQueueComponent.minigame.id == minigameId) {
                        count++
                    }
                }
            }

        return count
    }
}
