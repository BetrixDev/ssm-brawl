package net.ssmb.listeners

import com.github.shynixn.mccoroutine.bukkit.launch
import net.ssmb.SSMB
import net.ssmb.utils.t
import org.bukkit.GameMode
import org.bukkit.attribute.Attribute
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener : Listener {
    private val plugin = SSMB.instance

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        plugin.launch {
            val bannedRequest = plugin.api.playerIsIpBanned(player.address.hostName, player)

            if (bannedRequest.isBanned) {
                event.player.kick(t("titleScreen.banned"))
                return@launch
            }

            val playerData = plugin.api.playerBasicData(player)

            if (playerData.isBanned) {
                event.player.kick(t("titleScreen.banned"))
                return@launch
            }

            // Keep track of the player's most current username, so we can use it across different
            // services
            plugin.api.playerUpdatePlayerName(player)

            player.teleport(plugin.hub.spawnLocation)
            player.foodLevel = 20
            player.health = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
            player.gameMode = GameMode.SURVIVAL

            val joinMessage =
                if (playerData.firstTime) {
                    t("broadcast.player.joined")
                } else {
                    t("broadcast.player.joinedFirstTime")
                }

            plugin.hub.sendMessage(joinMessage)
        }
    }
}
