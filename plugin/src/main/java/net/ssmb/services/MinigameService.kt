package net.ssmb.services

import net.kyori.adventure.text.Component
import net.ssmb.SSMB
import net.ssmb.dtos.minigame.MinigameStartResponse
import org.bukkit.entity.Player
import java.util.*

class MinigameService(private val plugin: SSMB) {
    suspend fun tryStartMinigame(playerUuids: List<String>, minigameId: String) {
        val onlinePlayers = mutableListOf<Player>()
        val offlinePlayers = playerUuids.filter {
            val onlinePlayer = plugin.server.getPlayer(UUID.fromString(it))

            if (onlinePlayer != null) {
                !onlinePlayers.add(onlinePlayer)
            }

            onlinePlayer == null
        }

        if (offlinePlayers.isNotEmpty()) {
            onlinePlayers.forEach {
                it.sendMessage(Component.text("Tried to start the game with a player that was offline. You have been placed back into the queue"))
            }

            plugin.api.queueRemovePlayers(offlinePlayers)
            return
        }

        val startResponse = plugin.api.minigameStart(playerUuids, minigameId)

        if (startResponse is MinigameStartResponse.Error) {
            //
        } else if (startResponse is MinigameStartResponse.Success) {
            val startData = startResponse.value
        }
    }
}