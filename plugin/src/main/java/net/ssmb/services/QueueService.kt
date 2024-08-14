package net.ssmb.services

import com.github.shynixn.mccoroutine.bukkit.launch
import net.ssmb.SSMB
import net.ssmb.blockwork.annotations.Service
import org.bukkit.entity.Player

data class QueueRecord(val player: Player, val minigameId: String, val dateAdded: Long)

class AlreadyInQueueException(val minigame: String) : Exception()

class MinigameNotFoundException(val minigameInput: String) : Exception()

@Service
class QueueService(private val plugin: SSMB, private val minigames: MinigamesService) {
    private val queue = arrayListOf<QueueRecord>()

    fun addPlayerToQueue(player: Player, minigame: String) {
        val alreadyInQueue = queue.find { it.player == player }

        if (alreadyInQueue != null) {
            throw AlreadyInQueueException(alreadyInQueue.minigameId)
        }

        val minigameData =
            minigames.getMinigameData(minigame) ?: throw MinigameNotFoundException(minigame)

        val queueRecord = QueueRecord(player, minigame, System.currentTimeMillis())

        queue.add(queueRecord)

        // Offload checking if we can start a minigame onto a different thread
        plugin.launch {
            val playersInQueueForMinigame =
                queue.filter { it.minigameId == minigameData.id }.sortedBy { it.dateAdded }

            val playersNeededToStart = minigameData.teams * minigameData.playersPerTeam

            if (playersInQueueForMinigame.size < playersNeededToStart) return@launch

            // start game
            val playersToTake = playersInQueueForMinigame.subList(0, playersNeededToStart - 1)

        }
    }

    fun removePlayerFromQueue(player: Player) {
        queue.filter { it.player != player }
    }
}
