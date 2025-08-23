package dev.betrix.superSmashMobsBrawl.services

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.events.QueuePopEvent
import dev.betrix.superSmashMobsBrawl.models.brawlData.FfaMinigameDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.TeamBasedStocksMinigameDef
import gg.flyte.twilight.scheduler.repeatingTask
import java.util.logging.Logger
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

object QueueService : Manageable(), KoinComponent {
    private val logger: Logger by inject()
    private val minigameService: MinigameService by inject()

    private val queue = hashSetOf<QueueEntry>()

    init {
        runnables.add(
            repeatingTask(20) {
                // Periodically check if any queued minigame can start
                checkAllMinigamesCanStart()
            }
        )
    }

    fun addPlayer(player: Player, minigameDefinition: MinigameDef): Result<QueueEntry, QueueEntry> {
        val newEntry = QueueEntry(player, minigameDefinition)

        val existingEntry = queue.find { it.player == player }

        if (existingEntry != null) {
            return Err(existingEntry)
        }

        queue.add(newEntry)

        return Ok(newEntry)
    }

    fun removePlayer(player: Player): Result<QueueEntry, Unit> {
        val existingEntry = queue.find { it.player == player }

        return if (existingEntry != null) {
            queue.remove(existingEntry)
            Ok(existingEntry)
        } else {
            Err(Unit)
        }
    }

    fun getQueueEntry(player: Player): QueueEntry? {
        return queue.find { it.player == player }
    }

    fun getPlayersInQueue(minigameDef: MinigameDef): List<QueueEntry> {
        return queue.filter { it.minigame.id == minigameDef.id }
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

    private fun checkAllMinigamesCanStart() {
        // Snapshot the queue to determine which minigame types are present
        val snapshot = queue.toList()

        // Map unique minigame id -> definition
        val defsById = snapshot.groupBy { it.minigame.id }.mapValues { it.value.first().minigame }

        defsById.values.forEach { def -> tryStartMinigamesFor(def) }
    }

    private fun tryStartMinigamesFor(minigameDef: MinigameDef) {
        val requiredPlayers = getRequiredPlayersForMinigame(minigameDef)

        if (requiredPlayers <= 0) {
            logger.severe(
                "Minigame ${minigameDef.id} has invalid player requirement: $requiredPlayers"
            )
            return
        }

        // Filter out any players who may have entered a minigame meanwhile
        var available =
            getPlayersInQueue(minigameDef)
                .filter { !minigameService.isPlayerInMinigame(it.player) }
                .toMutableList()

        while (available.size >= requiredPlayers) {
            val playersToStart = available.take(requiredPlayers)

            // Remove chosen entries from the master queue
            playersToStart.forEach { queue.remove(it) }

            QueuePopEvent(minigameDef.id, playersToStart.map { it.player }).callEvent()

            // Drop the used players from the local list and continue if we can start more
            available = available.drop(requiredPlayers).toMutableList()
        }
    }
}
