package dev.betrix.supersmashmobsbrawl.managers

import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import dev.betrix.supersmashmobsbrawl.enums.LangEntry
import dev.betrix.supersmashmobsbrawl.managers.api.payloads.JoinQueue
import dev.betrix.supersmashmobsbrawl.managers.api.payloads.JoinQueueError
import dev.betrix.supersmashmobsbrawl.managers.api.payloads.JoinQueueResponse
import dev.betrix.supersmashmobsbrawl.managers.api.payloads.LeaveQueue
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

class QueueManager {
    private val plugin = SuperSmashMobsBrawl.instance
    private val api = plugin.api

    suspend fun tryAddPlayerToQueue(player: Player, modeId: String, isRanked: Boolean) {
        if (plugin.isShuttingDown) {
            player.sendMessage(plugin.lang.getComponent(LangEntry.QUEUE_SHUTDOWN, player))
            return
        }

        val response = api.addPlayerToQueue(player, modeId, isRanked)

        if (response is JoinQueue.Success) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("Successfully added to the queue"))
            if (response.value is JoinQueueResponse.StartGame) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("Attempting to start a game"))
                // Make sure players are online and still in queue by this point to avoid race conditions
                val players = response.value.playerUuids.map { Bukkit.getPlayer(UUID.fromString(it))!! }

                plugin.games.startGame(players, response.value.modeId, response.value.isRanked)
            }
        } else if (response is JoinQueue.Error) {
            when (response.value) {
                JoinQueueError.ALREADY_IN_QUEUE -> player.sendMessage(
                    MiniMessage.miniMessage().deserialize("You are already in a queue")
                )

                else -> player.sendMessage(
                    MiniMessage.miniMessage()
                        .deserialize("There was an error trying to add you to a queue. Please try again")
                )
            }
        }
    }

    suspend fun tryRemovePlayerFromQueue(player: Player) {
        val response = api.removePlayerFromQueue(player)

        if (response is LeaveQueue.Success) {
            player.sendMessage(plugin.lang.getComponent(LangEntry.QUEUE_LEFT, player))
        } else {
            player.sendMessage(plugin.lang.getComponent(LangEntry.QUEUE_LEFT_ERROR, player))
        }
    }
}