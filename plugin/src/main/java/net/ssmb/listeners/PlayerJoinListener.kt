package net.ssmb.listeners

import com.github.shynixn.mccoroutine.bukkit.launch
import net.ssmb.SSMB
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener : Listener {
    private val plugin = SSMB.instance

    @EventHandler(priority = EventPriority.LOWEST)
    suspend fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        plugin.launch {
            val bannedRequest = plugin.api.playerIsIpBanned(player.address.hostName, player)

            if (bannedRequest.isBanned) {
                event.player.kick(plugin.lang.getComponent("gui.kicked.ban"))
                return@launch
            }

            val playerData = plugin.api.playerBasicData(player)

            if (playerData.isBanned) {
                event.player.kick(plugin.lang.getComponent("gui.kicked.ban"))
                return@launch
            }

            player.teleport(plugin.hub.spawnLocation)
            player.foodLevel = 18

            val joinMessage =
                if (playerData.firstTime) {
                    plugin.lang.getComponent("chat.player.joinedserver.firsttime")
                } else {
                    plugin.lang.getComponent("chat.player.joinedserver")
                }

            plugin.hub.sendMessage(joinMessage)
        }
    }
}
