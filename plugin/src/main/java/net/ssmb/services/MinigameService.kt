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

    suspend fun tryStartMinigame(teams: List<List<String>>, minigameId: String) {
        val onlinePlayers = mutableListOf<MutableList<Player>>()
        val offlinePlayers =
            teams.filter { team ->
                val onlinePlayersInTeam = mutableListOf<Player>()

                val offlinePlayersInTeam =
                    team.filter { plr ->
                        val onlinePlayer = plugin.server.getPlayer(UUID.fromString(plr))

                        if (onlinePlayer != null) {
                            !onlinePlayersInTeam.add(onlinePlayer)
                        }

                        onlinePlayer == null
                    }

                onlinePlayers.add(onlinePlayersInTeam)

                offlinePlayersInTeam.isEmpty()
            }

        if (offlinePlayers.isNotEmpty()) {
            onlinePlayers.flatten().forEach {
                it.sendMessage(
                    Component.text(
                        "Tried to start the game with a player that was offline. You have been placed back into the queue"
                    )
                )
            }

            plugin.api.queueRemovePlayers(offlinePlayers.flatten())
            return
        }

        val startResponse = plugin.api.minigameStart(teams, minigameId)

        if (startResponse is MinigameStartResponse.Error) {
            onlinePlayers.flatten().forEach {
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
        return runningMinigames.find { it.teams.flatten().contains(player) } != null
    }

    fun areMinigamesOngoing(): Boolean {
        return runningMinigames.isNotEmpty()
    }
}
