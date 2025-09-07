package dev.betrix.superSmashMobsBrawl.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.components.*
import dev.betrix.superSmashMobsBrawl.models.brawlData.FfaMinigameDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.TeamBasedStocksMinigameDef
import dev.betrix.superSmashMobsBrawl.services.DataService

class QueueSystem(
    private val plugin: SuperSmashMobsBrawl = inject(),
    private val dataService: DataService = inject()
) : IntervalSystem(interval = Fixed(1f)) {

    // Not supporting parties queueing for minigames currently
    private val queuedEntities = family {
        all(InQueueComponent, PlayerComponent).none(InPartyComponent)
    }

    override fun onTick() {
        queuedEntities
            .groupBy { it[InQueueComponent].minigame.id }
            .mapKeys { dataService.getMinigame(it.key) }
            .filter {
                if (it.key == null) {
                    // Remove entity from queue because the minigame they were in was invalid
                    it.value.forEach { entity ->
                        plugin.logger.warning(
                            "Player was queued for minigame with id (${entity[InQueueComponent].minigame.id}) was is not valid"
                        )
                        entity.configure { it -= InQueueComponent }
                    }
                }

                return@filter it.key != null
            }
            .filter { (minigameDef, entities) ->
                entities.size >= getRequiredPlayersForMinigame(minigameDef!!)
            }
            .forEach { minigameDef, entities ->
                val minigameEntity = world.entity { it += MinigameComponent(minigameDef!!, playerEntities = entities) }

                entities.forEach { entity ->
                    val playerData = entity[PlayerDocumentComponent]

                    entity.configure {
                        it -= InQueueComponent
                        it += InMinigameComponent(minigameEntity, playerData.selectedKitId)
                    }
                }
            }
    }

    private fun getRequiredPlayersForMinigame(minigameDef: MinigameDef): Int {
        return when (minigameDef) {
            is TeamBasedStocksMinigameDef -> {
                minigameDef.playersPerTeam * minigameDef.amountOfTeams
            }

            is FfaMinigameDef -> {
                // Use the minimum to allow starting when the game defines it can
                minigameDef.minPlayers
            }
        }
    }
}
