package dev.betrix.superSmashMobsBrawl.services

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import gg.flyte.twilight.scheduler.delay
import org.bukkit.entity.Player
import kotlin.math.min

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

class QueueService {
    private val queue = hashSetOf<QueueEntry>()

    fun addPlayer(player: Player, minigame: MinigameDefinition): Result<QueueEntry, QueueEntry> {
        val newEntry = QueueEntry(player, minigame)

        val existingEntry = queue.find { it.player == player }

        if (existingEntry != null) {
            return Err(existingEntry)
        }

        queue.add(newEntry)

        // Delay checking for minigameCanStart so that the addPlayer method can return its result and relay the message to the player
        delay(20) {
            checkMinigameCanStart(minigame)
        }

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

    fun getPlayersInQueue(minigame: MinigameDefinition): List<QueueEntry> {
        return queue.filter { it.minigame.id == minigame.id }
    }

    private fun getRequiredPlayersForMinigame(minigame: MinigameDefinition): Int {
        return minigame.metadata.playersPerTeam * minigame.metadata.amountOfTeams
    }

    private fun checkMinigameCanStart(minigame: MinigameDefinition): Unit {
        val playersInQueue = getPlayersInQueue(minigame)
        val requiredPlayers = getRequiredPlayersForMinigame(minigame)

        if (playersInQueue.size < requiredPlayers) {
            return
        }

        val playersToStart = playersInQueue.take(requiredPlayers)
        playersToStart.forEach { queue.remove(it) }

        onMinigameCanStart(minigame, playersToStart)
    }

    private fun onMinigameCanStart(minigame: MinigameDefinition, queuedPlayers: List<QueueEntry>) {
        // Turn queue entries into teams and then maybe have a minigame service
        // to create the instance from definition and store that somewhere
    }
}