package dev.betrix.superSmashMobsBrawl.services

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import dev.betrix.superSmashMobsBrawl.minigames.PrototypingMinigame
import dev.betrix.superSmashMobsBrawl.models.brawlData.FfaMinigameDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.TeamBasedStocksMinigameDef
import java.util.UUID
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
    private val minigameService: MinigameService by inject()

    private val queue = hashSetOf<QueueEntry>()

    fun addPlayer(player: Player, minigameDefinition: MinigameDef): Result<QueueEntry, QueueEntry> {
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

    fun getPlayersInQueue(minigameDef: MinigameDef): List<QueueEntry> {
        return queue.filter { it.minigame.id == minigameDef.id }
    }

    private fun getRequiredPlayersForMinigame(minigameDef: MinigameDef): Int {
        return when (minigameDef) {
            is TeamBasedStocksMinigameDef -> {
                minigameDef.playersPerTeam * minigameDef.amountOfTeams
            }
            is FfaMinigameDef -> {
                minigameDef.maxPlayers
            }
        }
    }

    private fun checkMinigameCanStart(minigameDef: MinigameDef): Unit {
        val playersInQueue = getPlayersInQueue(minigameDef)
        val requiredPlayers = getRequiredPlayersForMinigame(minigameDef)

        if (playersInQueue.size < requiredPlayers) {
            return
        }

        val playersToStart = playersInQueue.take(requiredPlayers)
        playersToStart.forEach { queue.remove(it) }

        onMinigameCanStart(minigameDef, playersToStart)
    }

    private fun onMinigameCanStart(minigameDef: MinigameDef, queuedPlayers: List<QueueEntry>) {
        val entriesToUse =
            when (minigameDef) {
                is TeamBasedStocksMinigameDef -> {
                    val playersPerTeam = minigameDef.playersPerTeam
                    val amountOfTeams = minigameDef.amountOfTeams

                    // Take only the required number of players for all teams
                    val totalPlayersNeeded = playersPerTeam * amountOfTeams
                    queuedPlayers.take(totalPlayersNeeded)
                }
                is FfaMinigameDef -> {
                    queuedPlayers.take(minigameDef.maxPlayers)
                }
            }

        val gameId = UUID.randomUUID().toString()

        when (minigameDef) {
            is TeamBasedStocksMinigameDef -> {
                // TODO: Implement team-based start
                // val playersPerTeam = minigameDef.playersPerTeam
                // val amountOfTeams = minigameDef.amountOfTeams
            }
            is FfaMinigameDef -> {
                val players = entriesToUse.map { it.player }

                when (minigameDef.id) {
                    "prototyping" -> {
                        // Initialize and start the FFA minigame asynchronously in future edits
                        PrototypingMinigame(minigameDef.id, gameId, players)
                    }
                    else -> {
                        // No-op for unknown ids for now
                    }
                }
            }
        }

        // TODO: Figure out new minigame flow
    }
}
