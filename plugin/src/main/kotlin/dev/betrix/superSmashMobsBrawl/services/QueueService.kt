package dev.betrix.superSmashMobsBrawl.services

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import dev.betrix.superSmashMobsBrawl.models.brawlData.FfaMinigameDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.TeamBasedStocksMinigameDef
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// This will be easy to add party data to in the future if we want
data class QueueEntry(val player: Player, val minigame: MinigameDef) {
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

    fun getPlayersInQueue(minigame: MinigameDef): List<QueueEntry> {
        return queue.filter { it.minigame.id == minigame.id }
    }

    private fun getRequiredPlayersForMinigame(minigame: MinigameDef): Int {
        return when (minigame) {
            is TeamBasedStocksMinigameDef -> {
                minigame.playersPerTeam * minigame.amountOfTeams
            }
            is FfaMinigameDef -> {
                minigame.maxPlayers
            }
        }
    }

    private fun checkMinigameCanStart(minigame: MinigameDef): Unit {
        val playersInQueue = getPlayersInQueue(minigame)
        val requiredPlayers = getRequiredPlayersForMinigame(minigame)

        if (playersInQueue.size < requiredPlayers) {
            return
        }

        val playersToStart = playersInQueue.take(requiredPlayers)
        playersToStart.forEach { queue.remove(it) }

        onMinigameCanStart(minigame, playersToStart)
    }

    private fun onMinigameCanStart(minigame: MinigameDef, queuedPlayers: List<QueueEntry>) {
        val entriesToUse =
            when (minigame) {
                is TeamBasedStocksMinigameDef -> {
                    val playersPerTeam = minigame.playersPerTeam
                    val amountOfTeams = minigame.amountOfTeams

                    // Take only the required number of players for all teams
                    val totalPlayersNeeded = playersPerTeam * amountOfTeams
                    queuedPlayers.take(totalPlayersNeeded)
                }
                is FfaMinigameDef -> {
                    queuedPlayers.take(minigame.maxPlayers)
                }
            }

        // TODO: Figure out new minigame flow

        //        // Split into teams
        //        val teams =
        //            entriesToUse.chunked(playersPerTeam).map { chunk ->
        //                MinigameTeam(
        //                    chunk.map { it.player }.toMutableList(),
        //                    minigame.metadata.stocks,
        //                )
        //            }
        //
        //        // Remove these players from the queue
        //        entriesToUse.forEach { removePlayer(it.player) }
        //
        //        minigameService.initializeMinigameInstance(minigame, teams)
        //            .onFailure { err ->
        //                when (err) {
        //                    is MinigameInitError.PlayerAlreadyInMinigame -> {
        //                        val playersToAddBackToQueue =
        //                            entriesToUse.map { it.player }.filter {
        // !err.players.contains(it) }
        //
        //                        playersToAddBackToQueue.forEach { player ->
        //                            player.sendMessage(
        //                                mm(
        //                                    "<light_gray>There was an error. You have been added
        // back to the queue for ${minigame.name}</light_gray>"
        //                                )
        //                            )
        //                            addPlayer(player, minigame)
        //                        }
        //                    }
        //                }
        //            }
        //            .onSuccess { MinigameService.handleMinigameSetup(it) }
    }
}
