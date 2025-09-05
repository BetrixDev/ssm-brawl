package dev.betrix.superSmashMobsBrawl.services

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.World.Companion.family
import dev.betrix.superSmashMobsBrawl.components.InQueueComponent
import dev.betrix.superSmashMobsBrawl.components.PlayerComponent
import dev.betrix.superSmashMobsBrawl.extensions.ecsEntity
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// This will be easy to add party data to in the future if we want
data class QueueEntry(val player: Player, val minigame: MinigameDef, val partyId: String? = null) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (javaClass != other?.javaClass) {
            return false
        }

        other as QueueEntry

        return player == other.player
    }

    override fun hashCode(): Int {
        return player.hashCode()
    }
}

object QueueService : KoinComponent {
    private val ecsWorld: World by inject()

    fun addPlayer(player: Player, minigameDefinition: MinigameDef): Result<QueueEntry, QueueEntry> {
        val entity = player.ecsEntity ?: return Err(QueueEntry(player, minigameDefinition))

        with(ecsWorld) {
            // Check if player is already in queue
            if (entity.has(InQueueComponent)) {
                val existingInQueueComponent = entity[InQueueComponent]
                return Err(
                    QueueEntry(
                        player,
                        existingInQueueComponent.minigame,
                        existingInQueueComponent.partyId,
                    )
                )
            }

            // Add queue component to player entity
            entity.configure { it += InQueueComponent(minigameDefinition) }
        }

        return Ok(QueueEntry(player, minigameDefinition))
    }

    fun removePlayer(player: Player): Result<QueueEntry, Unit> {
        val entity = player.ecsEntity ?: return Err(Unit)

        with(ecsWorld) {
            return if (entity.has(InQueueComponent)) {
                val inQueueComponent = entity[InQueueComponent]
                val queueEntry =
                    QueueEntry(player, inQueueComponent.minigame, inQueueComponent.partyId)
                entity.configure { it -= InQueueComponent }
                Ok(queueEntry)
            } else {
                Err(Unit)
            }
        }
    }

    fun getQueueEntry(player: Player): QueueEntry? {
        val entity = player.ecsEntity ?: return null

        with(ecsWorld) {
            return if (entity.has(InQueueComponent)) {
                val inQueueComponent = entity[InQueueComponent]
                QueueEntry(player, inQueueComponent.minigame, inQueueComponent.partyId)
            } else {
                null
            }
        }
    }

    fun getPlayersInQueue(minigameDef: MinigameDef): List<QueueEntry> {
        val queuedPlayers = mutableListOf<QueueEntry>()

        with(ecsWorld) {
            // Get all entities with the required components
            val queueFamily = family { all(PlayerComponent, InQueueComponent) }
            queueFamily.forEach { entity ->
                val playerComponent = entity[PlayerComponent]
                val inQueueComponent = entity[InQueueComponent]

                if (inQueueComponent.minigame.id == minigameDef.id) {
                    queuedPlayers.add(
                        QueueEntry(
                            playerComponent.player,
                            inQueueComponent.minigame,
                            inQueueComponent.partyId,
                        )
                    )
                }
            }
        }

        return queuedPlayers
    }
}
