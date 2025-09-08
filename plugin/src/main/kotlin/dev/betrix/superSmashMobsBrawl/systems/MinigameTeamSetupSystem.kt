package dev.betrix.superSmashMobsBrawl.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import dev.betrix.superSmashMobsBrawl.components.InTeamComponent
import dev.betrix.superSmashMobsBrawl.components.MinigameComponent
import dev.betrix.superSmashMobsBrawl.components.MinigameState
import dev.betrix.superSmashMobsBrawl.components.StocksComponent
import dev.betrix.superSmashMobsBrawl.components.TeamMinigameComponent
import dev.betrix.superSmashMobsBrawl.models.TeamData
import dev.betrix.superSmashMobsBrawl.models.brawlData.TeamBasedStocksMinigameDef

/** Initializes team structures for team-based minigames before the world is loaded. */
class MinigameTeamSetupSystem : IteratingSystem(family { all(MinigameComponent) }) {

    override fun onTickEntity(entity: Entity) {
        val minigame = entity[MinigameComponent]
        if (minigame.state != MinigameState.LOADING_WORLD) return

        val def = minigame.minigame
        if (def !is TeamBasedStocksMinigameDef) return

        // Avoid re-initializing if already has teams
        if (entity.has(TeamMinigameComponent)) return

        val teams = mutableListOf<TeamData>()

        // Create teams
        repeat(def.amountOfTeams) { teamIndex ->
            val teamId = "team_${teamIndex + 1}"
            teams += TeamData(teamId = teamId, teamName = teamId, stocks = def.stocks)
        }

        // Assign players round-robin to teams and give individual stocks
        var idx = 0
        minigame.playerEntities.forEach { pEntity ->
            val team = teams[idx % teams.size]
            team.addMember(pEntity)
            pEntity.configure {
                it += InTeamComponent(team.teamId)
                it += StocksComponent(def.stocks, def.stocks)
            }
            idx++
        }

        // Attach TeamMinigameComponent to minigame entity
        entity.configure {
            it += TeamMinigameComponent(teams.associateBy { t -> t.teamId }.toMutableMap())
        }
    }
}
