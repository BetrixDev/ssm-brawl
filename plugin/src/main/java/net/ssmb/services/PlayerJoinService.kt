package net.ssmb.services

import net.ssmb.SSMB
import net.ssmb.blockwork.Blockwork
import net.ssmb.blockwork.annotations.Service
import net.ssmb.blockwork.interfaces.OnStart
import net.ssmb.lifecycles.OnPlayerJoined
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

@Service
class PlayerJoinService(private val plugin: SSMB) : OnStart, Listener {
    private val listeners = arrayListOf<OnPlayerJoined>()

    override fun onStart() {
        plugin.server.pluginManager.registerEvents(this, plugin)

        Blockwork.modding.onListenerAdded<OnPlayerJoined> { listeners.add(it) }

        Blockwork.modding.onListenerRemoved<OnPlayerJoined> { listeners.remove(it) }

        Bukkit.getServer().onlinePlayers.forEach { plr ->
            listeners.forEach { it.onPlayerJoined(plr) }
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        listeners.forEach { it.onPlayerJoined(player) }
    }
}
