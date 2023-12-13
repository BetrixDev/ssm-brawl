package dev.betrix.supersmashmobsbrawl.listeners

import com.github.shynixn.mccoroutine.bukkit.launch
import dev.betrix.supersmashmobsbrawl.SSMBPlayer
import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener : Listener {
    private val plugin = SuperSmashMobsBrawl.instance

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        plugin.hub.teleportPlayer(event.player)

        plugin.launch {
            plugin.api.fetchPlayerData(event.player)
            SSMBPlayer.addPlayer(event.player)
        }
    }
}