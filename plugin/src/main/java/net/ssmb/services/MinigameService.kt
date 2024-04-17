package net.ssmb.services

import java.util.*
import net.kyori.adventure.text.Component
import net.ssmb.SSMB
import net.ssmb.dtos.minigame.MinigameStartResponse
import net.ssmb.minigames.IMinigame
import net.ssmb.minigames.constructMinigameFromData
import org.bukkit.entity.Player

class MinigameService {
    private val plugin = SSMB.instance

    private val runningMinigames = arrayListOf<IMinigame>()

    suspend fun tryStartMinigame(playerUuids: List<String>, minigameId: String) {
        val onlinePlayers = mutableListOf<Player>()
        val offlinePlayers =
            playerUuids.filter {
                val onlinePlayer = plugin.server.getPlayer(UUID.fromString(it))

                if (onlinePlayer != null) {
                    !onlinePlayers.add(onlinePlayer)
                }

                onlinePlayer == null
            }

        if (offlinePlayers.isNotEmpty()) {
            onlinePlayers.forEach {
                it.sendMessage(
                    Component.text(
                        "Tried to start the game with a player that was offline. You have been placed back into the queue"
                    )
                )
            }

            plugin.api.queueRemovePlayers(offlinePlayers)
            return
        }

        val startResponse = plugin.api.minigameStart(playerUuids, minigameId)

        if (startResponse is MinigameStartResponse.Error) {
            onlinePlayers.forEach {
                it.sendMessage(Component.text("An error occurred while trying to start the game."))
            }
        } else if (startResponse is MinigameStartResponse.Success) {
            val startData = startResponse.value
            val minigame = constructMinigameFromData(onlinePlayers, startData)

            minigame.initializeMinigame()

            runningMinigames.add(minigame)
        }
    }

    fun onMinigameEnd(minigame: IMinigame) {
        runningMinigames.remove(minigame)
    }

    fun isPlayerInMinigame(player: Player): Boolean {
        return runningMinigames.find { it.players.contains(player) } != null
    }
}
