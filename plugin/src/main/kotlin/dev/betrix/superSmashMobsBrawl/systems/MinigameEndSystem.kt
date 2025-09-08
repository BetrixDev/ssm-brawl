package dev.betrix.superSmashMobsBrawl.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.components.InMinigameComponent
import dev.betrix.superSmashMobsBrawl.components.MinigameComponent
import dev.betrix.superSmashMobsBrawl.components.MinigameState
import dev.betrix.superSmashMobsBrawl.components.PlayerComponent
import dev.betrix.superSmashMobsBrawl.components.TeamMinigameComponent
import dev.betrix.superSmashMobsBrawl.models.brawlData.FfaMinigameDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.TeamBasedStocksMinigameDef
import dev.betrix.superSmashMobsBrawl.services.HubService
import dev.betrix.superSmashMobsBrawl.services.LangService
import org.bukkit.GameMode

/** Determines when minigames end and performs wrap-up/teleport. */
class MinigameEndSystem(
    private val plugin: SuperSmashMobsBrawl = inject(),
    private val lang: LangService = inject(),
    private val hub: HubService = inject(),
) : IteratingSystem(family { all(dev.betrix.superSmashMobsBrawl.components.MinigameComponent) }) {

    override fun onTickEntity(entity: Entity) {
        val minigame = entity[dev.betrix.superSmashMobsBrawl.components.MinigameComponent]
        if (minigame.state != MinigameState.ONGOING) return

        when (val def = minigame.minigame) {
            is FfaMinigameDef -> handleFfa(entity, minigame)
            is TeamBasedStocksMinigameDef -> handleTeamStocks(entity, minigame)
        }
    }

    private fun handleFfa(
        entity: Entity,
        minigame: dev.betrix.superSmashMobsBrawl.components.MinigameComponent,
    ) {
        val active = activePlayers(minigame)
        if (active.size <= 1) {
            endMinigame(entity, minigame, winnerName = active.firstOrNull()?.name ?: "Unknown")
        }
    }

    private fun handleTeamStocks(
        entity: Entity,
        minigame: dev.betrix.superSmashMobsBrawl.components.MinigameComponent,
    ) {
        val teams = entity.getOrNull(TeamMinigameComponent) ?: return
        val teamsWithStocks = teams.getAllTeams().filter { it.stocks > 0 }
        val teamsWithActive =
            teams.getAllTeams().filter { team ->
                team.members.any { member ->
                    val p = member[PlayerComponent].player
                    p.isOnline && p.gameMode == GameMode.SURVIVAL
                }
            }
        if (teamsWithStocks.size == 1 && teamsWithActive.size <= 1) {
            val winners = teamsWithStocks.first().members.map { it[PlayerComponent].player.name }
            val msg =
                lang.t("messages.minigames.teamBasedStocks.winner") {
                    "winners" to winners.joinToString(", ")
                    "minigameId" to minigame.minigame.id
                }
            plugin.server.onlinePlayers.forEach { it.sendMessage(msg) }
            endMinigame(entity, minigame)
        }
    }

    private fun activePlayers(
        minigame: dev.betrix.superSmashMobsBrawl.components.MinigameComponent
    ) =
        minigame.playerEntities
            .map { it[PlayerComponent].player }
            .filter { p -> p.isOnline && p.gameMode == GameMode.SURVIVAL }

    private fun endMinigame(
        entity: Entity,
        minigame: dev.betrix.superSmashMobsBrawl.components.MinigameComponent,
        winnerName: String? = null,
    ) {
        // prevent duplicate execution
        if (minigame.state == MinigameState.ENDING || minigame.state == MinigameState.CLEANUP)
            return
        minigame.state = MinigameState.ENDING

        // Teleport participants and spectators to hub and detach them from the minigame
        minigame.playerEntities.forEach { e ->
            hub.tryTeleportToDefaultHub(e[PlayerComponent].player)
            e.configure { it -= InMinigameComponent }
        }
        minigame.spectatorEntities.forEach { e ->
            hub.tryTeleportToDefaultHub(e[PlayerComponent].player)
            e.configure { it -= InMinigameComponent }
        }
        minigame.disconnectedPlayers.clear()

        // Transition to cleanup. Further world cleanup can be handled by a dedicated
        // service/system.
        minigame.state = MinigameState.CLEANUP
    }
}
