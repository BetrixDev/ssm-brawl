package net.ssmb.services

import java.util.*
import net.kyori.adventure.text.Component
import net.ssmb.SSMB
import net.ssmb.dtos.minigame.BukkitTeamData
import net.ssmb.dtos.minigame.MinigameStartRequest
import net.ssmb.dtos.minigame.MinigameStartResponse
import net.ssmb.dtos.queue.AddPlayerSuccess
import net.ssmb.minigames.IMinigame
import net.ssmb.minigames.constructMinigameFromData
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class MinigameService {
    private val plugin = SSMB.instance

    private val runningMinigames = arrayListOf<IMinigame>()

    suspend fun tryStartMinigame(
        teams: List<AddPlayerSuccess.StartGame.TeamEntry>,
        minigameId: String
    ) {
        val onlinePlayers = arrayListOf<Player>()
        val offlinePlayers = arrayListOf<Player>()

        teams.forEach { team ->
            team.players.forEach { plrUuid ->
                val player = Bukkit.getPlayer(UUID.fromString(plrUuid))

                if (player != null) {
                    onlinePlayers.add(player)
                } else {
                    offlinePlayers.add(Bukkit.getPlayer(UUID.fromString(plrUuid))!!)
                }
            }
        }

        if (offlinePlayers.isNotEmpty()) {
            onlinePlayers.forEach {
                it.sendMessage(
                    Component.text(
                        "Tried to start the game with a player that was offline. You have been placed back into the queue"
                    )
                )
            }

            plugin.api.queueRemovePlayers(offlinePlayers.map { it.uniqueId.toString() })
            return
        }

        val teamsPayload = arrayListOf<MinigameStartRequest.TeamEntry>()

        teams.forEach { teamsPayload.add(MinigameStartRequest.TeamEntry(it.id, it.players)) }

        val startResponse = plugin.api.minigameStart(teamsPayload, minigameId)

        if (startResponse is MinigameStartResponse.Error) {
            onlinePlayers.forEach {
                it.sendMessage(Component.text("An error occurred while trying to start the game."))
            }
        } else if (startResponse is MinigameStartResponse.Success) {
            val startData = startResponse.value

            val teamData =
                startData.teams.map { team ->
                    val players = arrayListOf<Player>()

                    team.players.forEach { plr ->
                        players.add(Bukkit.getPlayer(UUID.fromString(plr.uuid))!!)
                    }

                    BukkitTeamData(team.teamId, players)
                }

            val minigame = constructMinigameFromData(teamData, startData)

            minigame.initializeMinigame()

            runningMinigames.add(minigame)
        }
    }

    fun onMinigameEnd(minigame: IMinigame) {
        runningMinigames.remove(minigame)
    }

    fun isPlayerInMinigame(player: Player): Boolean {
        return runningMinigames.find {
            it.teams.map { team -> team.players }.flatten().contains(player)
        } != null
    }

    fun areMinigamesOngoing(): Boolean {
        return runningMinigames.isNotEmpty()
    }
}
