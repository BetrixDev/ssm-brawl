package dev.betrix.superSmashMobsBrawl.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.components.InMinigameComponent
import dev.betrix.superSmashMobsBrawl.components.PlayerComponent
import dev.betrix.superSmashMobsBrawl.components.QueueComponent
import dev.betrix.superSmashMobsBrawl.events.QueuePopEvent
import dev.betrix.superSmashMobsBrawl.models.brawlData.FfaMinigameDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.TeamBasedStocksMinigameDef
import org.bukkit.entity.Player

class QueueSystem(
    private val plugin: SuperSmashMobsBrawl = inject()
) : IteratingSystem(
    family { all(PlayerComponent, QueueComponent) }
) {
    private var lastMatchmakingCheck = 0L
    private val matchmakingInterval = 1000L // Check every second (20 ticks)

    override fun onTick() {
        super.onTick()

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastMatchmakingCheck >= matchmakingInterval) {
            checkForMatches()
            lastMatchmakingCheck = currentTime
        }
    }

    override fun onTickEntity(entity: Entity) {
        // Individual entity processing if needed
        // Currently just used to maintain the family query
    }

    private fun checkForMatches() {
        // Group queued players by minigame type
        val queuedPlayersByMinigame = mutableMapOf<String, MutableList<Pair<Entity, Player>>>()

        // Iterate through all entities with PlayerComponent and QueueComponent
        family.forEach { entity ->
            val playerComponent = entity[PlayerComponent]
            val queueComponent = entity[QueueComponent]
            val player = playerComponent.player

            // Skip players who are already in a minigame
            if (entity.has(InMinigameComponent)) {
                return@forEach
            }

            val minigameId = queueComponent.minigame.id
            queuedPlayersByMinigame.getOrPut(minigameId) { mutableListOf() }
                .add(entity to player)
        }

        // Check each minigame type for possible matches
        queuedPlayersByMinigame.forEach { (minigameId, playersWithEntities) ->
            if (playersWithEntities.isEmpty()) return@forEach

            val minigameDef = playersWithEntities.first().first[QueueComponent].minigame
            val requiredPlayers = getRequiredPlayersForMinigame(minigameDef)

            if (requiredPlayers <= 0) {
                plugin.logger.severe("Minigame $minigameId has invalid player requirement: $requiredPlayers")
                return@forEach
            }

            // Start as many games as possible with available players
            val availablePlayers = playersWithEntities.toMutableList()
            while (availablePlayers.size >= requiredPlayers) {
                val playersToStart = availablePlayers.take(requiredPlayers)

                // Remove queue components from selected players
                playersToStart.forEach { (entity, _) ->
                    entity.configure {
                        it -= QueueComponent
                    }
                }

                // Fire the queue pop event
                val players = playersToStart.map { it.second }
                QueuePopEvent(minigameId, players).callEvent()

                // Remove used players from available list
                availablePlayers.removeAll(playersToStart)
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
