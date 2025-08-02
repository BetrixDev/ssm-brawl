package dev.betrix.superSmashMobsBrawl.services

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import dev.betrix.superSmashMobsBrawl.models.MinigameTeam
import dev.betrix.superSmashMobsBrawl.utils.mm
import org.bukkit.entity.Player

// This will be easy to add party data to in the future if we want
data class QueueEntry(val player: Player, val minigame: MinigameDefinition) {
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

object QueueService {
    private val queue = hashSetOf<QueueEntry>()

    fun addPlayer(
        player: Player,
        minigameDefinition: MinigameDefinition,
    ): Result<QueueEntry, QueueEntry> {
        val newEntry = QueueEntry(player, minigameDefinition)

        val existingEntry = queue.find { it.player == player }

        if (existingEntry != null) {
            return Err(existingEntry)
        }

        queue.add(newEntry)

        checkMinigameCanStart(minigameDefinition)

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

    fun getPlayersInQueue(minigameDefinition: MinigameDefinition): List<QueueEntry> {
        return queue.filter { it.minigame.id == minigameDefinition.id }
    }

    private fun getRequiredPlayersForMinigame(minigameDefinition: MinigameDefinition): Int {
        return minigameDefinition.metadata.playersPerTeam *
            minigameDefinition.metadata.amountOfTeams
    }

    private fun checkMinigameCanStart(minigameDefinition: MinigameDefinition): Unit {
        val playersInQueue = getPlayersInQueue(minigameDefinition)
        val requiredPlayers = getRequiredPlayersForMinigame(minigameDefinition)

        if (playersInQueue.size < requiredPlayers) {
            return
        }

        val playersToStart = playersInQueue.take(requiredPlayers)
        playersToStart.forEach { queue.remove(it) }

        onMinigameCanStart(minigameDefinition, playersToStart)
    }

    private fun onMinigameCanStart(
        minigameDefinition: MinigameDefinition,
        queuedPlayers: List<QueueEntry>,
    ) {
        val playersPerTeam = minigameDefinition.metadata.playersPerTeam
        val amountOfTeams = minigameDefinition.metadata.amountOfTeams

        // Take only the required number of players for all teams
        val totalPlayersNeeded = playersPerTeam * amountOfTeams
        val entriesToUse = queuedPlayers.take(totalPlayersNeeded)

        // Split into teams
        val teams =
            entriesToUse.chunked(playersPerTeam).map { chunk ->
                MinigameTeam(
                    chunk.map { it.player }.toMutableList(),
                    minigameDefinition.metadata.stocks,
                )
            }

        // Remove these players from the queue
        entriesToUse.forEach { removePlayer(it.player) }

        MinigameService.initializeMinigameInstance(minigameDefinition, teams)
            .onFailure { err ->
                when (err) {
                    is MinigameInitError.PlayerAlreadyInMinigame -> {
                        val playersToAddBackToQueue =
                            entriesToUse.map { it.player }.filter { !err.players.contains(it) }

                        playersToAddBackToQueue.forEach { player ->
                            player.sendMessage(
                                mm(
                                    "<light_gray>There was an error. You have been added back to the queue for ${minigameDefinition.name}</light_gray>"
                                )
                            )
                            addPlayer(player, minigameDefinition)
                        }
                    }
                }
            }
            .onSuccess { MinigameService.handleMinigameSetup(it) }
    }
}
