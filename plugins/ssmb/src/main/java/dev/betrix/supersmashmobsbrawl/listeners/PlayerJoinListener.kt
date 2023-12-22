package dev.betrix.supersmashmobsbrawl.listeners

import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import dev.betrix.supersmashmobsbrawl.enums.LangEntry
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener : Listener {
    private val plugin = SuperSmashMobsBrawl.instance

    @EventHandler
    suspend fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        player.locale()

        plugin.hub.teleportPlayer(player)
        player.foodLevel = 18

        plugin.api.fetchPlayerData(player, true)

        val joinMessage = plugin.lang.getComponent(LangEntry.SERVER_PLAYER_JOINED, player)
        event.joinMessage(joinMessage)
    }
}